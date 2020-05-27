package struct;

import java.io.File;
import java.io.InputStream;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.binary.XSSFBSharedStringsTable;
import org.apache.poi.xssf.binary.XSSFBSheetHandler;
import org.apache.poi.xssf.binary.XSSFBStylesTable;
import org.apache.poi.xssf.eventusermodel.XSSFBReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;

public class Importer {

	public static void importFile(File file) {
		try (OPCPackage opcPackage = OPCPackage.open(file)) {

			String xml = null;
			{

				XSSFBReader reader = new XSSFBReader(opcPackage);

				XSSFBSharedStringsTable sst = new XSSFBSharedStringsTable(opcPackage);
				XSSFBStylesTable stylesTable = reader.getXSSFBStylesTable();
				XSSFBReader.SheetIterator iterator = (XSSFBReader.SheetIterator) reader.getSheetsData();


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

			Document document = DocumentHelper.parseText(xml);

			System.out.println(document.getRootElement().getName());


		} catch (Exception e) {
			e.printStackTrace();
		}
	}

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
