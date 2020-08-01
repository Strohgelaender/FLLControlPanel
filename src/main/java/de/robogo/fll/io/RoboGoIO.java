package de.robogo.fll.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.DefaultBaseTypeLimitingValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import de.robogo.fll.util.Directories;
import javafx.concurrent.Task;

public abstract class RoboGoIO extends Task<Void> {

	protected static final int MAX_STEPS = 6;

	protected final File dataDir;
	protected final File teamFile;
	protected final File slotsFile;
	protected final File juryFile;
	protected final File tableFile;
	protected final File eventNameFile;
	protected final File activeTimeFile;
	protected final ObjectMapper objectMapper;

	RoboGoIO() {
		dataDir = Directories.getDataDir();
		teamFile = new File(dataDir, "teams.txt");
		slotsFile = new File(dataDir, "slots.txt");
		juryFile = new File(dataDir, "juries.txt");
		tableFile = new File(dataDir, "tables.txt");
		eventNameFile = new File(dataDir, "eventName.txt");
		activeTimeFile = new File(dataDir, "activeTime.txt");
		objectMapper = new ObjectMapper();
		objectMapper.activateDefaultTyping(new DefaultBaseTypeLimitingValidator());
		objectMapper.registerModule(new JavaTimeModule());
	}

	protected void updateProgress(int progress) {
		updateProgress(progress, MAX_STEPS);
	}

	public static String readResourceFile(String filename) {
		InputStream inputStream = RoboGoIO.class.getClassLoader().getResourceAsStream(filename);
		if (inputStream == null)
			return "";

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
			return reader.lines().collect(Collectors.joining("\n"));
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}
	}

}
