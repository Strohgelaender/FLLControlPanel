package de.robogo.fll.io;

import java.io.File;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.DefaultBaseTypeLimitingValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import de.robogo.fll.util.Directories;
import javafx.concurrent.Task;

public abstract class RoboGoIO extends Task<Void> {

	private static final int MAX_STEPS = 5;

	protected final File dataDir;
	protected final File teamFile;
	protected final File slotsFile;
	protected final File juryFile;
	protected final File tableFile;
	protected final File eventNameFile;
	protected final ObjectMapper objectMapper;

	RoboGoIO() {
		dataDir = Directories.getDataDir();
		teamFile = new File(dataDir, "teams.txt");
		slotsFile = new File(dataDir, "slots.txt");
		juryFile = new File(dataDir, "juries.txt");
		tableFile = new File(dataDir, "tables.txt");
		eventNameFile = new File(dataDir, "eventName.txt");
		objectMapper = new ObjectMapper();
		objectMapper.activateDefaultTyping(new DefaultBaseTypeLimitingValidator());
		objectMapper.registerModule(new JavaTimeModule());
	}

	protected void updateProgress(int progress) {
		updateProgress(progress, MAX_STEPS);
	}

}
