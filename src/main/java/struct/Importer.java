package struct;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class Importer {

	public static void importFile(File file) {
		try (FileInputStream fis = new FileInputStream(file)) {
			XSSFWorkbook workbook = new XSSFWorkbook(fis);

			for (int i = 0; i < workbook.getNumberOfSheets(); i++) {

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
