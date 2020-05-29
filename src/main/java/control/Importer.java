package control;

import java.io.File;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.binary.XSSFBSharedStringsTable;
import org.apache.poi.xssf.binary.XSSFBSheetHandler;
import org.apache.poi.xssf.binary.XSSFBStylesTable;
import org.apache.poi.xssf.eventusermodel.XSSFBReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import teams.Team;

public class Importer {

	public static void importFile(File file) {
		if (file == null || file.isDirectory() || !file.exists() || !file.canRead())
			//TODO Fehlermeldung
			return;

		try (OPCPackage opcPackage = OPCPackage.open(file)) {

			String xml = null;
			{

				XSSFBReader reader = new XSSFBReader(opcPackage);

				XSSFBSharedStringsTable sst = new XSSFBSharedStringsTable(opcPackage);
				XSSFBStylesTable stylesTable = reader.getXSSFBStylesTable();
				XSSFBReader.SheetIterator iterator = (XSSFBReader.SheetIterator) reader.getSheetsData();


				//TODO das geht bestimmt irgendwie effizienter ohne Schleife...
				while (iterator.hasNext()) {
					InputStream is = iterator.next();

					if (!iterator.getSheetPart().getPartName().getName().endsWith("sheet25.bin"))
						continue;

					String name = iterator.getSheetName();
					System.out.println(name);

					TestSheetHandler testSheetHandler = new TestSheetHandler();
					testSheetHandler.startSheet(name);

					XSSFBSheetHandler sheetHandler = new XSSFBSheetHandler(is, stylesTable, iterator.getXSSFBSheetComments(), sst, testSheetHandler, new DataFormatter(), false);
					sheetHandler.parse();
					testSheetHandler.endSheet();

					xml = testSheetHandler.toString();
					break;
				}
			}

			if (xml == null)
				//TODO show Exeption to User
				return;

			System.out.println(xml);

			Document document = DocumentBuilderFactory.newDefaultInstance().newDocumentBuilder().parse(new InputSource(new StringReader(xml)));

			NodeList rows = document.getChildNodes().item(0).getChildNodes();

			int juryTableRow = -1;
			//Schleife 1: Suche nach Jury-Tabelle
			//Jury-Tabelle = 2. Tabelle mit "#1" in H-Spalte
			juryLoop:
			for (int i = 0; i < rows.getLength(); i++) {
				Node row = rows.item(i);
				if (!row.getNodeName().equals("tr"))
					continue;

				NodeList cells = row.getChildNodes();

				for (int j = 0; j < cells.getLength(); j++) {
					Node cell = cells.item(j);
					if (cell.getAttributes() == null)
						continue;
					String cellName = cell.getAttributes().getNamedItem("ref").getTextContent();
					String columnIndex = cellName.replaceAll("[0-9]", "");
					if (columnIndex.charAt(0) < 72)
						//Spalte vor H -> weitersuchen
						continue;

					if (columnIndex.length() > 1 || columnIndex.charAt(0) > 72) { //72 = H ASCI
						//Keine Werte in H -> abbrechen
						break;
					}

					if (!cell.getTextContent().equals("#1"))
						break;

					juryTableRow = i;
					break juryLoop;
				}
			}

			if (juryTableRow == -1) {
				//TODO show Exeption to User
				System.err.println("Jury Table not found!");
				return;
			}

			//Tabelle über Zeitplan-Matrix = Teamnamen

			NodeList teams = rows.item(juryTableRow - 2).getChildNodes();

			Node tt;
			List<Team> teamList = new ArrayList<>();
			for (int i = 1; i < teams.getLength(); i += 2) {
				tt = teams.item(i);
				if (tt.getAttributes() == null)
					continue;
				String ttName = tt.getTextContent().trim();
				if (tt.getTextContent().trim().equals("Teams"))
					continue;
				if (ttName == "" || ttName == null)
					break;
				teamList.add(new Team(ttName, i / 2));

			}
			Controller.setTeams(teamList);

			//Zeitplan importieren

			int rgrone = juryTableRow;

			//Schleife 1: Suche nach Robot-Game-Tabelle
			//RG-Tabelle = 2. Tabelle mit "#1" in H-Spalte
			for (int i = rgrone + 1; i < rows.getLength(); i++) {
				Node row = rows.item(i);
				if (!row.getNodeName().equals("tr"))
					continue;

				NodeList cells = row.getChildNodes();

				for (int j = 0; j < cells.getLength(); j++) {
					Node cell = cells.item(j);
					if (cell.getAttributes() == null)
						continue;
					String cellName = cell.getAttributes().getNamedItem("ref").getTextContent();
					String columnIndex = cellName.replaceAll("[0-9]", "");
					if (columnIndex.charAt(0) < 72)
						//Spalte vor H -> weitersuchen
						continue;

					if (columnIndex.length() > 1 || columnIndex.charAt(0) > 72) { //72 = H ASCI
						//Keine Werte in H -> abbrechen
						break;
					}

					if (!cell.getTextContent().equals("#1"))
						break;

					rgrone = i;
					break juryLoop;
				}
			}

			if (rgrone == juryTableRow) {
				//TODO show Exeption to User
				System.err.println("Jury Table not found!");
				return;
			}

			// Zeilen unter zweitem / dritten / viertem #1: Robotgame-Runden

			for (int i = rgrone; i != 0; ) {

				NodeList times = rows.item(rgrone + 2 * i).getChildNodes();

				for (int j = 0; j < cells.getLength(); j++) {
					Node cell = cells.item(j);
					if (cell.getAttributes() == null)
						continue;
					String cellName = cell.getAttributes().getNamedItem("ref").getTextContent();
					String columnIndex = cellName.replaceAll("[0-9]", "");
					if (columnIndex.charAt(0) < 72)
						//Spalte vor H -> weitersuchen
						continue;

					if (columnIndex.length() > 1 || columnIndex.charAt(0) > 72) { //72 = H ASCI
						//Keine Werte in H -> abbrechen
						break;
					}

					if (!cell.getTextContent().equals("#1"))
						break;

				}
			}


		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//TODO
	private static class TestSheetHandler implements XSSFSheetXMLHandler.SheetContentsHandler {
		private final StringBuilder sb = new StringBuilder();

		public void startSheet(String sheetName) {
			sb.append("<sheet name=\"").append(sheetName).append("\">");
		}

		public void endSheet() {
			sb.append("</sheet>");
		}

		@Override
		public void startRow(int rowNum) {
			sb.append("\n<tr num=\"").append(rowNum).append("\">");
		}

		@Override
		public void endRow(int rowNum) {
			//sb.append("\n</tr num=\"").append(rowNum).append("\">");
			sb.append("\n</tr>");
		}

		@Override
		public void cell(String cellReference, String formattedValue, XSSFComment comment) {
			formattedValue = (formattedValue == null) ? "" : formattedValue;
			if (comment == null) {
				sb.append("\n\t<td ref=\"").append(cellReference).append("\">").append(formattedValue).append("</td>");
			} else {
				sb.append("\n\t<td ref=\"").append(cellReference).append("\">")
						.append(formattedValue)
						.append("<span type=\"comment\" author=\"")
						.append(comment.getAuthor()).append("\">")
						.append(comment.getString().toString().trim()).append("</span>")
						.append("</td>");
			}
		}

		@Override
		public void headerFooter(String text, boolean isHeader, String tagName) {
			/*if (isHeader) {
				sb.append("<header tagName=\"").append(tagName).append("\">").append(text).append("</header>");
			} else {
				sb.append("<footer tagName=\"").append(tagName).append("\">").append(text).append("</footer>");
			}*/
		}

		@Override
		public String toString() {
			return sb.toString();
		}
	}
}
