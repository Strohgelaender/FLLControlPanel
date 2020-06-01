package de.robogo.fll.control;

import static de.robogo.fll.control.FLLController.getTableByNumber;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

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
import org.xml.sax.SAXException;

import de.robogo.fll.entity.JuryTimeSlot;
import de.robogo.fll.entity.RobotGameTimeSlot;
import de.robogo.fll.entity.RoundMode;
import de.robogo.fll.entity.Team;
import de.robogo.fll.entity.TimeSlot;
import javafx.application.Platform;
import javafx.concurrent.Task;

public class Importer extends Task<Void> {

	private static final int maxStatus = 8;
	private final File file;
	private final Runnable runLater;

	public Importer(final File file, Runnable runLater) {
		this.file = file;
		this.runLater = runLater;
	}

	@Override
	protected Void call() throws Exception {
		updateProgress(0.5, maxStatus);

		if (file == null || file.isDirectory() || !file.exists() || !file.canRead())
			//TODO Fehlermeldung
			return null;

		String xml = null;
		try (OPCPackage opcPackage = OPCPackage.open(file)) {

			updateProgress(1, maxStatus);

			XSSFBReader reader = new XSSFBReader(opcPackage);

			XSSFBSharedStringsTable sst = new XSSFBSharedStringsTable(opcPackage);
			XSSFBStylesTable stylesTable = reader.getXSSFBStylesTable();
			XSSFBReader.SheetIterator iterator = (XSSFBReader.SheetIterator) reader.getSheetsData();

			updateProgress(2, maxStatus);

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
		} catch (Exception e) {
			//TODO handle (v.a FileNotFoundException kann nicht zugreifen mit Hinweis)
			e.printStackTrace();
			return null;
		}

		if (xml == null)
			//TODO show Exeption to User
			return null;

		updateProgress(3, maxStatus);

		Document document = null;
		try {
			document = DocumentBuilderFactory.newDefaultInstance().newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
		} catch (SAXException | IOException | ParserConfigurationException e) {
			//TODO handle
			e.printStackTrace();
		}

		if (document == null)
			return null;

		updateProgress(4, maxStatus);

		NodeList rows = document.getChildNodes().item(0).getChildNodes();
		int lastIndexInName = StringUtils.lastIndexOf(rows.item(21).getChildNodes().item(1).getTextContent(), "-");
		String nameOfCompetition = rows.item(21).getChildNodes().item(1).getTextContent().substring(0, lastIndexInName).trim();
		FLLController.setEventName(nameOfCompetition);
		List<TimeSlot> timeSlots = new ArrayList<>();

		//Import Locations
		char[] juryColumn = {'I', 'I', 'I', 'I'};
		String[] juryRegex = {"^TR\\S$", "R", "T", "F"};
		int[] juryTypeRows = findmultipleRowsWithContent(rows, 0, juryColumn, juryRegex);
		String[][] juryRooms = new String[4][4]; // in this order : testRoundRooms, robotDesignRooms,teamworkRooms, researchRooms
		for (int i = 0; i < 3; i++) {
			NodeList juryList = rows.item(juryTypeRows[i]).getChildNodes();
			int numberOfJury = 0;
			for (int j = 1; i < juryList.getLength(); i += 2) {
				Node juryCell = juryList.item(j);
				String column = getColumnIndex(juryCell);
				if (column.charAt(0) < 'J') continue;
				if (juryCell.getTextContent().replaceAll("[0-9]", "").matches(juryRegex[i])) continue;
				juryRooms[i][numberOfJury] = juryCell.getTextContent();
				numberOfJury++;
			}
		}
		juryRooms[0][1] = juryRooms[0][0];
		juryRooms[0][2] = juryRooms[0][0];
		juryRooms[0][3] = juryRooms[0][0];

		updateProgress(5, maxStatus);

		int juryTableRow = findRowWithContent(rows, 0, 'H', "^#1$");

		if (juryTableRow == -1) {
			//TODO show Exeption to User
			System.err.println("Jury Table not found!");
			return null;
		}

		//Table above judging-sessions = Team names

		NodeList teams = rows.item(juryTableRow - 2).getChildNodes();

		List<Team> teamList = new ArrayList<>();
		for (int i = 3; i < teams.getLength(); i += 2) {
			Node tt = teams.item(i);
			if (tt.getAttributes() == null)
				continue;
			String ttName = tt.getTextContent();
			if (StringUtils.isEmpty(ttName))
				break;
			teamList.add(new Team(ttName.trim(), i / 2));
		}
		FLLController.setTeams(teamList);

		updateProgress(5, maxStatus);

		//Import Testrounds and Jury Sessions

		int loopEnd = findRowWithContent(rows, juryTableRow + 2, 'E', " - ") - 2;
		outerLoop:
		for (int i = juryTableRow + 2; i < loopEnd; i += 2) {

			NodeList juryList = rows.item(i).getChildNodes();
			String stringTime = juryList.item(1).getTextContent();
			if (stringTime.length() == 4)
				stringTime = "0" + stringTime;
			LocalTime time = LocalTime.parse(stringTime);
			for (int j = 3; j < juryList.getLength(); j += 2) {
				int tempteam = 0;
				Node cell = juryList.item(j);
				String name = getColumnIndex(cell);
				String jurySession = juryList.item(j).getTextContent();
				String juryTypeString = jurySession.replaceAll("[0-9]", "");
				int juryNumber;
				try {
					juryNumber = Integer.parseInt(jurySession.replaceAll("[A-Z]", ""));
				} catch (NumberFormatException e) { //Pause
					continue outerLoop;
				}

				int juryTypeIndex = 0;
				for (int k = 0; k < 4; k++) {
					if (juryRegex[k].matches(juryTypeString)) {
						juryTypeIndex = k;
						break;
					}
				}
				if (name.length() == 1)
					tempteam = name.charAt(0) - 72;

				if (name.length() == 2)
					tempteam = name.charAt(1) - 46;

				Team t1 = tempteam < teamList.size() ? teamList.get(tempteam) : null;
				timeSlots.add(new JuryTimeSlot(t1, time, JuryTimeSlot.JuryType.values()[juryTypeIndex], juryRooms[juryTypeIndex][juryNumber - 1], juryNumber));

			}


		}

		updateProgress(6, maxStatus);

		//Begin importing Robot Game times

		int rgrone = findRowWithContent(rows, juryTableRow + 1, 'H', "^#1$");

		if (rgrone == -1) {
			//TODO show Exeption to User
			System.err.println("RobotGameTimeTable not found!");
			return null;
		}


		char[] roboclumn = {'E', 'E', 'E'};
		String[] roboregex = {" - ", " - ", " - ",};

		int[] endRoundRows = findmultipleRowsWithContent(rows, rgrone, roboclumn, roboregex);

		// Lines in the next 3 section below the 2nd "#1": Robotgame-preliminary Rounds


		generateRoboSlots(rows, rgrone, endRoundRows[0], 1, timeSlots, teamList);
		generateRoboSlots(rows, endRoundRows[0], endRoundRows[1], 2, timeSlots, teamList);
		generateRoboSlots(rows, endRoundRows[1], endRoundRows[2], 3, timeSlots, teamList);

		updateProgress(7, maxStatus);

		//Final rounds in tables below

		int firstfinal = findRowWithContent(rows, endRoundRows[2], 'H', "^[A-Z]*1[A-Z]*$");
		if (rows.item(firstfinal).getChildNodes().getLength() > 25) { //if quarter-final exists, there are more than 25 nodes in the header of the timetable
			char[] finalcolumns = {'E', 'E', 'E'};
			String[] nextFinalIndicator = {" - ", " - ", " - "};
			int[] nextFinal = findmultipleRowsWithContent(rows, firstfinal, finalcolumns, nextFinalIndicator);
			generateRoboSlots(rows, firstfinal, nextFinal[0], 4, timeSlots, teamList);
			generateRoboSlots(rows, nextFinal[0], nextFinal[1], 5, timeSlots, teamList);
			generateRoboSlots(rows, nextFinal[1], nextFinal[2], 6, timeSlots, teamList);
		} else { //no quarter-finals
			char[] finalcolumns = {'E', 'E'};
			String[] nextFinalIndicator = {" - ", " - "};
			int[] nextfinal = findmultipleRowsWithContent(rows, firstfinal, finalcolumns, nextFinalIndicator);
			generateRoboSlots(rows, firstfinal, nextfinal[0], 5, timeSlots, teamList);
			generateRoboSlots(rows, nextfinal[0], nextfinal[1], 6, timeSlots, teamList);
		}

		FLLController.setTimeSlots(timeSlots);

		System.out.println("Import fertig");
		updateProgress(8, maxStatus);
		Platform.runLater(runLater);
		return null;
	}

	private static String getColumnIndex(Node cell) {
		String cellName = cell.getAttributes().getNamedItem("ref").getTextContent();
		return cellName.replaceAll("[0-9]", "");
	}

	private static int findRowWithContent(NodeList nodes, int startRow, char column, String regex) {
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

				String columnIndex = getColumnIndex(cell);

				if (columnIndex.charAt(0) < column)
					//column is on the left of selected column -> continue with next column
					continue;


				if (columnIndex.length() > 1 || columnIndex.charAt(0) > column)
					//no values in selected column -> continue outerloop for next row
					break;

				if (!cell.getTextContent().matches(regex))
					//wrong content -> continue searching in next row (outer loop)
					break;

				//Content found
				retVal = i;
				break outerLoop;
			}
		}
		return retVal;
	}

	static int[] findmultipleRowsWithContent(NodeList nodes, int startRow, char[] column, String[] regex) {
		int[] results = new int[Integer.max(column.length, regex.length)];
		int lastRow = startRow;
		for (int i = 0; i < results.length; i++) {
			results[i] = findRowWithContent(nodes, lastRow + 1, column[i], regex[i]);
			if (results[i] != -1) lastRow = results[i];
		}

		return results;
	}

	static void generateRoboSlots(NodeList matches, int tableHead, int nextTableHead, int round, List<TimeSlot> roboSlots, List<Team> teamList) {

		for (int i = tableHead + 2; i < nextTableHead - 2; i += 2) {

			NodeList match = matches.item(i).getChildNodes();
			LocalTime temptime = null;
			int[] tempteam = new int[2];
			int[] temptable = new int[2];

			for (int j = 1; j < match.getLength(); j += 2) {
				Node cell = match.item(j);
				String name = getColumnIndex(cell);
				if (name.charAt(0) == 'D') {
					temptime = LocalTime.parse(cell.getTextContent());
					continue;
				}

				if (name.length() == 1)
					tempteam[j / 2 - 1] = name.charAt(0) - 72;

				if (name.length() == 2)
					tempteam[j / 2 - 1] = name.charAt(1) - 46;

				temptable[j / 2 - 1] = Integer.parseInt(cell.getTextContent());
			}
			Team t1 = tempteam[0] < teamList.size() ? teamList.get(tempteam[0]) : null;
			Team t2 = tempteam[1] < teamList.size() ? teamList.get(tempteam[1]) : null;
			roboSlots.add(new RobotGameTimeSlot(t1, t2, getTableByNumber(temptable[0]), getTableByNumber(temptable[1]), temptime, RoundMode.values()[round]));
		}
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
