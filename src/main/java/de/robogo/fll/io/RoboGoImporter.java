package de.robogo.fll.io;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;

import de.robogo.fll.control.FLLController;

public class RoboGoImporter extends RoboGoIO {

	public void importAll() {
		try {
			importJuries();
			importTable();
			importTeam();
			importTimeSlot();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private <T> List<T> importStuff(File file) throws IOException {
		return objectMapper.readValue(file, new TypeReference<>() {
		});
	}

	public void importTimeSlot() throws IOException {
		FLLController.setTimeSlots(importStuff(slotsFile));
	}

	public void importTeam() throws IOException {
		FLLController.setTimeSlots(importStuff(teamFile));
	}

	public void importTable() throws IOException {
		FLLController.setTimeSlots(importStuff(tableFile));
	}

	public void importJuries() throws IOException {
		FLLController.setTimeSlots(importStuff(juryFile));
	}

	@Override
	protected Void call() {
		importAll();
		return null;
	}
}
