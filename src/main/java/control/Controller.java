package control;

import java.util.ArrayList;
import java.util.List;

import entity.RobotGameTimeSlot;
import entity.Table;
import teams.Team;

public class Controller {

	private static final List<Team> teams = new ArrayList<>();

	private static final List<Table> tables = new ArrayList<>();

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
		Controller.tables.clear();
		Controller.tables.addAll(tables);
	}

	public static void setTeams(final List<Team> teams) {
		Controller.teams.clear();
		Controller.teams.addAll(teams);
	}

	public static void setTimeSlots(final List<RobotGameTimeSlot> timeSlots) {
		Controller.timeSlots.clear();
		Controller.timeSlots.addAll(timeSlots);
	}

	public static Team getTeamByName(String name) {
		for (Team t : getTeams()) {
			if (t.getName().equals(name))
				return t;
		}
		return null;
	}
}
