package de.robogo.fll.io;

import java.io.File;
import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.databind.type.TypeFactory;

import de.robogo.fll.control.FLLController;
import de.robogo.fll.entity.Jury;
import de.robogo.fll.entity.Table;
import de.robogo.fll.entity.Team;
import de.robogo.fll.entity.timeslot.TimeSlot;

public class RoboGoImporter extends RoboGoIO {

	public void importAll() {
		try {
			updateProgress(0);
			importJuries();
			updateProgress(1);
			importTable();
			updateProgress(2);
			importTeam();
			updateProgress(3);
			importTimeSlot();
			updateProgress(4);
			importEventName();
			updateProgress(5);
			importActiveTime();
		} catch (IOException e) {
			e.printStackTrace();
		}
		updateProgress(MAX_STEPS);
	}

	private <T> List<T> importStuff(File file, Class<T> tClass) throws IOException {
		if (file.exists()) {
			TypeFactory typeFactory = TypeFactory.defaultInstance();
			return objectMapper.readValue(file, typeFactory.constructCollectionType(ArrayList.class, tClass));
		}
		return Collections.emptyList();
	}

	public void importTimeSlot() throws IOException {
		List<TimeSlot> timeSlots = importStuff(slotsFile, TimeSlot.class);
		if (!timeSlots.isEmpty())
			FLLController.setTimeSlots(timeSlots);
	}

	public void importTeam() throws IOException {
		List<Team> teams = importStuff(teamFile, Team.class);
		if (!teams.isEmpty())
			FLLController.setTeams(teams);
	}

	public void importTable() throws IOException {
		List<Table> tables = importStuff(tableFile, Table.class);
		if (!tables.isEmpty())
			FLLController.setTables(tables);
	}

	public void importJuries() throws IOException {
		List<Jury> juries = importStuff(juryFile, Jury.class);
		if (!juries.isEmpty())
			FLLController.setJuries(juries);
	}

	public void importEventName() throws IOException {
		if (eventNameFile.exists()) {
			FLLController.setEventName(objectMapper.readValue(eventNameFile, String.class));
		}
	}

	public void importActiveTime() throws IOException {
		if (activeTimeFile.exists()) {
			FLLController.setActiveTime(objectMapper.readValue(activeTimeFile, LocalTime.class));
		}
	}

	@Override
	protected Void call() {
		importAll();
		return null;
	}
}
