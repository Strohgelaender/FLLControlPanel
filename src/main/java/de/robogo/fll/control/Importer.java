package de.robogo.fll.control;

import java.io.File;
import java.io.InputStream;
import java.io.StringReader;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.binary.XSSFBSharedStringsTable;
import org.apache.poi.xssf.binary.XSSFBSheetHandler;
import org.apache.poi.xssf.binary.XSSFBStylesTable;
import org.apache.poi.xssf.eventusermodel.XSSFBReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import de.robogo.fll.entity.RobotGameTimeSlot;
import de.robogo.fll.entity.Team;

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

			int juryTableRow = findRowWithContent(rows, 0, 'H', "#1");

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
				String ttName = tt.getTextContent();
				if (tt.getTextContent().trim().equals("Teams"))
					continue;
				if (ttName == null || ttName.equals(""))
					break;
				teamList.add(new Team(ttName.trim(), i / 2));
			}
			FLLController.setTeams(teamList);

			//Zeitplan importieren

			int rgrone = juryTableRow;

			//Schleife 1: Suche nach Robot-Game-Tabelle
			//RG-Tabelle = 2. Tabelle mit "#1" in H-Spalte
			timesLoop:
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
					break timesLoop;
				}
			}

			if (rgrone == juryTableRow) {
				//TODO show Exeption to User
				System.err.println("RobotGameTimeTable not found!");
				return;
			}

			// Zeilen unter zweitem / dritten / viertem #1: Robotgame-Runden
			int rground = 1;
			List<RobotGameTimeSlot>[] roboslots = new ArrayList[3];


			roundLoop:
			for (int i = rgrone + 2; i != 0; i += 2) {

				NodeList times = rows.item(i).getChildNodes();
				LocalTime temptime;
				int[] tempteam = new int[2];
				int[] temptable = new int[2];

				for (int j = 1; j < times.getLength(); j += 2) {
					Node cell = times.item(j);
					if (cell.getAttributes() == null)
						continue;
					String cellName = cell.getAttributes().getNamedItem("ref").getTextContent();
					String columnIndex = cellName.replaceAll("[0-9]", "");

					if (columnIndex.charAt(0) == 66 && rground < 3) { //B = 66 in ASCII
						rground++;
						continue roundLoop;
					}
					if (columnIndex.charAt(0) == 66 && rground == 3) break roundLoop;
					if (columnIndex.charAt(0) == 68) {  // D = 68 in ASCII
						cell.getTextContent();
						// temptime.set(****)
						continue;
					}
					if (columnIndex.length() == 1) tempteam[j / 2 - 1] = columnIndex.charAt(0) - 71;
					if (columnIndex.length() == 2) tempteam[j / 2 - 1] = columnIndex.charAt(1) - 45;
					temptable[j / 2 - 1] = Integer.parseInt(cell.getTextContent());


				}

				roboslots[rground - 1].add(new RobotGameTimeSlot(teamList.get(tempteam[0]), teamList.get(tempteam[1]), temptable[0], temptable[1]));
			}


		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static int findRowWithContent(NodeList nodes, int startRow, char column, String content) {
		int retVal = -1;
		outerLoop:
		for (int i = startRow; i < nodes.getLength(); i++) {
			Node row = nodes.item(i);
			if (!row.getNodeName().equals("tr"))
				continue;

			NodeList cells = row.getChildNodes();

			for (int j = 0; j < cells.getLength(); j++) {
				Node cell = cells.item(j);
				if (cell.getAttributes() == null)
					continue;

				String cellName = cell.getAttributes().getNamedItem("ref").getTextContent();
				String columnIndex = cellName.replaceAll("[0-9]", "");

				if (columnIndex.charAt(0) < column)
					//Spalte vor gesuchter Column -> weitersuchen
					continue;


				if (columnIndex.length() > 1 || columnIndex.charAt(0) > column)
					//Keine Werte in gesuchter Column -> innere Schleife abbrechen
					break;

				if (!cell.getTextContent().equals(content))
					//falscher Inhalt -> in nächster Reihe weitersuchen (äußere Schleife)
					break;

				//Inhalt gefunden
				retVal = i;
				break outerLoop;
			}
		}
		return retVal;
	}

	public static void importScores(XSSFWorkbook workbook) {
		XSSFSheet sheet = workbook.getSheet("Robot Game Score");

		for (int i = 0; i < 28; i++) {
			XSSFRow row = sheet.getRow(3 + i);

			if (row == null)
				break;

			String teamRaw = row.getCell(0).getStringCellValue();
			int nameEnd = StringUtils.lastIndexOf(teamRaw, "[");
			String teamName = StringEscapeUtils.unescapeHtml3(StringEscapeUtils.unescapeHtml4(teamRaw.substring(0, nameEnd).trim()));

			Team team = FLLController.getTeamByName(teamName);

			if (team == null) {
				System.out.println("No Team with name " + teamName + " found!");
				//TODO handle (how?)
				//continue;
				//for debug:
				team = new Team(teamName, i);
				FLLController.getTeams().add(team);
			}

			int game1 = (int) row.getCell(1).getNumericCellValue();
			int game2 = (int) row.getCell(2).getNumericCellValue();
			int game3 = (int) row.getCell(3).getNumericCellValue();
			int qf = (int) row.getCell(5).getNumericCellValue();
			int rank = (int) row.getCell(7).getNumericCellValue();

			team.setRound1(game1);
			team.setRound2(game2);
			team.setRound3(game3);
			team.setQF(qf);
			team.setRank(rank);
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
