package de.robogo.fll.io;

import java.io.File;
import java.io.IOException;

import de.robogo.fll.control.FLLController;
import de.robogo.fll.io.RoboGoIO;

public class Exporter extends RoboGoIO {

	public void exportAll() {
		System.out.println("Exporting Data to:");
		System.out.println(dataDir.getAbsolutePath());
		try {
			exportTeamData();
			exportSlots();
			exportJuries();
			exportTables();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void exportStuff(File f, Object o) throws IOException {
		if (!f.exists())
			f.createNewFile();
		objectMapper.writeValue(f, o);
	}

	public void exportTeamData() throws IOException {
		exportStuff(teamFile, FLLController.getTeams());
	}

	public void exportSlots() throws IOException {
		exportStuff(slotsFile, FLLController.getTimeSlots());
	}

	public void exportJuries() throws IOException {
		exportStuff(juryFile, FLLController.getJuries());
	}

	public void exportTables() throws IOException {
		exportStuff(tableFile, FLLController.getTables());
	}


	//Screens

	//Screen1

	//Other Config (active Slot etc)


}
