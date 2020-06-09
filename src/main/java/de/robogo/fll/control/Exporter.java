package de.robogo.fll.control;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.robogo.fll.entity.Team;
import de.robogo.fll.util.Directories;

public class Exporter {

	private final File dataDir;
	private final File teamFile;
	private final File slotsFile;
	private final File juryFile;
	private final File tableFile;
	private final ObjectMapper objectMapper;

	public Exporter() {
		dataDir = Directories.getDataDir();
		teamFile = new File(dataDir, "teams.txt");
		slotsFile = new File(dataDir, "slots.txt");
		juryFile = new File(dataDir, "juries.txt");
		tableFile = new File(dataDir, "tables.txt");
		objectMapper = new ObjectMapper();
	}

	public void exportAll() {
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
