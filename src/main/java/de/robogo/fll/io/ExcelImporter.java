package de.robogo.fll.io;

import static de.robogo.fll.control.FLLController.JURY_SLOT_DURATION;
import static de.robogo.fll.control.FLLController.ROBOT_GAME_SLOT_DURATION;
import static de.robogo.fll.control.FLLController.getTableByNumber;
import static de.robogo.fll.entity.Jury.JuryType.TestRound;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

import de.robogo.fll.control.FLLController;
import de.robogo.fll.entity.Jury;
import de.robogo.fll.entity.RoundMode;
import de.robogo.fll.entity.Team;
import de.robogo.fll.entity.timeslot.JuryPauseTimeSlot;
import de.robogo.fll.entity.timeslot.JuryTimeSlot;
import de.robogo.fll.entity.timeslot.RobotGamePauseTimeSlot;
import de.robogo.fll.entity.timeslot.RobotGameTimeSlot;
import de.robogo.fll.entity.timeslot.TimeSlot;
import javafx.concurrent.Task;

public class ExcelImporter extends Task<Void> {

	private static final int maxStatus = 8;
	private final File file;

	public ExcelImporter(final File file) {
		this.file = file;
	}

	@Override
	protected Void call() throws ImportFailedException {
		try {
			updateProgress(0.5, maxStatus);

			if (file == null || file.isDirectory() || !file.exists() || !file.canRead())
				throw new ImportFailedException("the selected file is not supported!");

			String xml = null;
			String lcXml = null;
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

					if (iterator.getSheetPart().getPartName().getName().endsWith("sheet25.bin")) {
						xml = readExcelBinarySheet(is, iterator, sst, stylesTable);
					} else if (iterator.getSheetPart().getPartName().getName().endsWith("sheet26.bin") && iterator.getSheetName().contains("LC")) {
						lcXml = readExcelBinarySheet(is, iterator, sst, stylesTable);
					}
					if (xml != null && lcXml != null)
						break;
				}
			} catch (Exception e) {
				//TODO translation
				throw new ImportFailedException("Die Datei konnte nicht eingelesn werden. Dies passiert typischerweise, wenn Sie die Datei noch zuätzlich in Excel offen haben. Bitte stellen Sie sicher, dass kein anderes Programm auf diese Datei zugreift.", e);
			}

			if (xml == null)
				throw new ImportFailedException("Die Datei konnte nicht eingelesen werden.");

			updateProgress(3, maxStatus);

			Document document;
			try {
				document = DocumentBuilderFactory.newDefaultInstance().newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
			} catch (SAXException | IOException | ParserConfigurationException e) {
				throw new ImportFailedException("Die eingelesene Datei ist fehlerhaft.", e);
			}

			if (document == null)
				throw new NullPointerException("Die eingelesene Datei ist fehlerhaft.");

			updateProgress(4, maxStatus);

			NodeList rows = document.getChildNodes().item(0).getChildNodes();
			int lastIndexInName = StringUtils.lastIndexOf(rows.item(21).getChildNodes().item(1).getTextContent(), "-");
			String nameOfCompetition = rows.item(21).getChildNodes().item(1).getTextContent().substring(0, lastIndexInName).trim();
			FLLController.setEventName(nameOfCompetition);
			List<TimeSlot> timeSlots = new ArrayList<>();
			//Import Locations
			FLLController.getJuries().clear();
			char[] juryColumn = {'I', 'I', 'I', 'I'};
			String[] juryRegex = {"^TR\\S$", "R", "T", "F"};
			int[] juryTypeRows = findMultipleRowsWithContent(rows, 0, juryColumn, juryRegex);
			juryRegex[0] = "TR";
			//reading data in this order : testRoundRooms, robotDesignRooms,teamworkRooms, researchRooms
			for (int i = 0; i < 4; i++) {
				NodeList juryList = rows.item(juryTypeRows[i]).getChildNodes();
				int numberOfJury = 1;
				for (int j = 1; j < juryList.getLength(); j += 2) {
					Node juryCell = juryList.item(j);
					String column = getColumnIndex(juryCell);
					if (column.charAt(0) < 'K')
						continue;
					if (juryCell.getTextContent().replaceAll("[0-9]", "").matches(juryRegex[i]))
						continue;

					FLLController.addJuries(new Jury(Jury.JuryType.values()[i], numberOfJury, juryCell.getTextContent()));
					numberOfJury++;
				}
			}
			//Testround Jurys 2-4
			String trroom = Objects.requireNonNull(FLLController.getJury(TestRound, 1)).getRoom();
			FLLController.addJuries(new Jury(TestRound, 2, trroom), new Jury(TestRound, 3, trroom), new Jury(TestRound, 4, trroom));


			updateProgress(5, maxStatus);

			int juryTableRow = findRowWithContent(rows, 0, 'H', "^#1$");

			if (juryTableRow == -1) {
				throw new ImportFailedException("In der Datei konnten keine Informationen zu den Jury-Bewertugen gefunden werden. Bitte stellen sie sicher, dass die Datei ein korrekter Zeitplan ist.");
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
			LocalTime t = null;
			outerLoop:
			for (int i = juryTableRow + 2; i < loopEnd; i += 2) {

				NodeList juryList = rows.item(i).getChildNodes();
				String stringTime = juryList.item(1).getTextContent();
				if (stringTime.length() == 4)
					stringTime = "0" + stringTime;
				t = LocalTime.parse(stringTime);
				for (int j = 3; j < juryList.getLength(); j += 2) {
					int tempTeam = 0;
					Node cell = juryList.item(j);
					String name = getColumnIndex(cell);
					String jurySession = juryList.item(j).getTextContent();
					if (name.length() == 1)
						tempTeam = name.charAt(0) - 72;

					if (name.length() == 2)
						tempTeam = name.charAt(1) - 46;

					Jury jury = FLLController.getJuryByIdentifier(jurySession.trim());
					if (jury == null) {
						addPauseSlot(0, timeSlots, t);
						continue outerLoop;
					}

					Team t1 = tempTeam < teamList.size() ? teamList.get(tempTeam) : null;
					timeSlots.add(new JuryTimeSlot(t1, t, jury));

				}
			}
			assert t != null;
			addPauseSlot(0, timeSlots, t.plusMinutes(JURY_SLOT_DURATION));


			updateProgress(6, maxStatus);

			//Begin importing Robot Game times

			int rgrone = findRowWithContent(rows, juryTableRow + 1, 'H', "^#1$");

			if (rgrone == -1) {
				throw new ImportFailedException("In der Datei konnten keine Informationen zu den RobotGames gefunden werden. Bitte stellen sie sicher, dass die Datei ein korrekter Zeitplan ist.");
			}


			char[] roboclumn = {'E', 'E', 'E'};
			String[] roboregex = {" - ", " - ", " - ",};

			int[] endRoundRows = findMultipleRowsWithContent(rows, rgrone, roboclumn, roboregex);

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
				int[] nextFinal = findMultipleRowsWithContent(rows, firstfinal, finalcolumns, nextFinalIndicator);
				generateRoboSlots(rows, firstfinal, nextFinal[0], 4, timeSlots, teamList);
				generateRoboSlots(rows, nextFinal[0], nextFinal[1], 5, timeSlots, teamList);
				generateRoboSlots(rows, nextFinal[1], nextFinal[2], 6, timeSlots, teamList);
			} else { //no quarter-finals
				char[] finalcolumns = {'E', 'E'};
				String[] nextFinalIndicator = {" - ", " - "};
				int[] nextfinal = findMultipleRowsWithContent(rows, firstfinal, finalcolumns, nextFinalIndicator);
				generateRoboSlots(rows, firstfinal, nextfinal[0], 5, timeSlots, teamList);
				generateRoboSlots(rows, nextfinal[0], nextfinal[1], 6, timeSlots, teamList);
			}

			if (lcXml != null) {
				//TODO LiveChallenge Import
			}

			FLLController.setTimeSlots(timeSlots);

			System.out.println("Import fertig");
			updateProgress(8, maxStatus);
			return null;
		} catch (Exception e) {
			if (e instanceof ImportFailedException)
				throw e;
			e.printStackTrace();
			throw new ImportFailedException(e);
		}
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

	private static int[] findMultipleRowsWithContent(NodeList nodes, int startRow, char[] column, String[] regex) {
		int[] results = new int[Integer.max(column.length, regex.length)];
		int lastRow = startRow;
		for (int i = 0; i < results.length; i++) {
			results[i] = findRowWithContent(nodes, lastRow + 1, column[i], regex[i]);
			if (results[i] != -1) lastRow = results[i];
		}

		return results;
	}

	private static void generateRoboSlots(NodeList matches, int tableHead, int nextTableHead, int round, List<TimeSlot> roboSlots, List<Team> teamList) {
		LocalTime tempTime = null;
		for (int i = tableHead + 2; i < nextTableHead - 2; i += 2) {

			NodeList match = matches.item(i).getChildNodes();
			tempTime = null;
			int[] tempTeam = new int[2];
			int[] tempTable = new int[2];

			for (int j = 1; j < match.getLength(); j += 2) {
				Node cell = match.item(j);
				String name = getColumnIndex(cell);
				if (name.charAt(0) == 'D') {
					tempTime = LocalTime.parse(cell.getTextContent());
					continue;
				}

				if (name.length() == 1)
					tempTeam[j / 2 - 1] = name.charAt(0) - 72;

				if (name.length() == 2)
					tempTeam[j / 2 - 1] = name.charAt(1) - 46;

				tempTable[j / 2 - 1] = Integer.parseInt(cell.getTextContent());
			}
			Team t1 = tempTeam[0] < teamList.size() ? teamList.get(tempTeam[0]) : null;
			Team t2 = tempTeam[1] < teamList.size() ? teamList.get(tempTeam[1]) : null;
			roboSlots.add(new RobotGameTimeSlot(t1, t2, getTableByNumber(tempTable[0]), getTableByNumber(tempTable[1]), tempTime, RoundMode.values()[round]));
		}
		assert tempTime != null;
		addPauseSlot(round, roboSlots, tempTime.plusMinutes(ROBOT_GAME_SLOT_DURATION));
	}

	private static void addPauseSlot(int r, List<TimeSlot> s, LocalTime t) {
		if (r == 0)
			s.add(new JuryPauseTimeSlot(t));
		else s.add(new RobotGamePauseTimeSlot(t, RoundMode.values()[r]));
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


	//TODO nemove txt-attributs (how?) [might break Importer!]
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

	private static String readExcelBinarySheet(InputStream is, XSSFBReader.SheetIterator iterator, XSSFBSharedStringsTable sst, XSSFBStylesTable stylesTable) throws IOException {
		String name = iterator.getSheetName();
		System.out.println(name);

		TestSheetHandler testSheetHandler = new TestSheetHandler();
		testSheetHandler.startSheet(name);

		XSSFBSheetHandler sheetHandler = new XSSFBSheetHandler(is, stylesTable, iterator.getXSSFBSheetComments(), sst, testSheetHandler, new DataFormatter(), false);
		sheetHandler.parse();
		testSheetHandler.endSheet();

		return testSheetHandler.toString();
	}
}
