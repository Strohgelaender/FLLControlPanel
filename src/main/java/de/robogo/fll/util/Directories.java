package de.robogo.fll.util;

import java.io.File;
import java.io.IOException;

import net.harawata.appdirs.AppDirsFactory;

public class Directories {

	public static File getDataDir() {
		return checkDir(AppDirsFactory.getInstance().getSiteDataDir("FLLControlPanel", null, "RoboGO"));
	}

	public static File checkDir(final String path) {
		return checkDir(new File(path));
	}

	public static File checkDir(final File dir) {
		// First we check if a directory exists
		boolean dirExists = dir.exists();

		if (!dirExists) {
			// Second we check if there is a shortcut
			final File filelnk = new File(dir.getAbsolutePath() + ".lnk");
			final boolean linkExists = filelnk.exists();

			if (linkExists) {
				try {
					final LnkParser parse = new LnkParser(filelnk);
					final String target = parse.getRealFilename();

					return checkDir(target);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		if (dir.mkdirs()) {
			System.out.printf("Created directory: %s %n", dir.getAbsolutePath());
		}

		return dir;
	}
}
