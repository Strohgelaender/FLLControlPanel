package de.robogo.fll.io;

import java.io.File;
import java.io.IOException;

import de.robogo.fll.control.FLLController;

public class RoboGoExporter extends RoboGoIO {

	public void exportAll() {
		System.out.println("Exporting Data to:");
		System.out.println(dataDir.getAbsolutePath());
		updateProgress(0);
		try {
			exportTeamData();
			updateProgress(1);
			exportSlots();
			updateProgress(2);
			exportJuries();
			updateProgress(3);
			exportTables();
			updateProgress(4);
			exportEventName();
			updateProgress(5);
			exportActiveTime();
		} catch (IOException e) {
			e.printStackTrace();
		}
		updateProgress(MAX_STEPS);
	}

	private void exportStuff(File f, Object o) throws IOException {
		if (!f.exists())
			f.createNewFile(); //TODO handle Exception
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

	public void exportEventName() throws IOException {
		exportStuff(eventNameFile, FLLController.getEventName());
	}

	public void exportActiveTime() throws IOException {
		exportStuff(activeTimeFile, FLLController.getActiveTime());
	}

	@Override
	protected Void call() {
		exportAll();
		return null;
	}


	//Screens

	//Screen1

	//Other Config (active Slot etc)


}
