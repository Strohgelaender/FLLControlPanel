package de.robogo.fll.control;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.robogo.fll.entity.RobotGameTimeSlot;
import de.robogo.fll.entity.Table;
import de.robogo.fll.entity.Team;

public class FLLController {

	//TODO set in Importer!
	private static String eventName = "FLL Regio Hochtesthausen";

	private static final List<Team> teams = new ArrayList<>();

	private static final List<Table> tables = new ArrayList<>(Arrays.asList(new Table("1"), new Table("2"), new Table("4"), new Table("4")));

	private static final List<RobotGameTimeSlot> timeSlots = new ArrayList<>();

	public static List<Team> getTeams() {
		return teams;
	}

	public static List<RobotGameTimeSlot> getTimeSlots() {
		return timeSlots;
	}

	public static List<Table> getTables() {
		return tables;
	}

	public static void setTables(final List<Table> tables) {
		FLLController.tables.clear();
		FLLController.tables.addAll(tables);
	}

	public static void setTeams(final List<Team> teams) {
		FLLController.teams.clear();
		FLLController.teams.addAll(teams);
	}

	public static void setTimeSlots(final List<RobotGameTimeSlot> timeSlots) {
		FLLController.timeSlots.clear();
		FLLController.timeSlots.addAll(timeSlots);
	}

	public static Team getTeamByName(String name) {
		for (Team t : getTeams()) {
			if (t.getName().equals(name))
				return t;
		}
		return null;
	}

	public static Table getTableByNumber(int num) {
		return getTables().get(num - 1);
	}

	public static String getEventName() {
		return eventName;
	}

	public static void setEventName(final String eventName) {
		FLLController.eventName = eventName;
	}
}
