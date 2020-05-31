package de.robogo.fll.control;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import de.robogo.fll.entity.JuryTimeSlot;
import de.robogo.fll.entity.RobotGameTimeSlot;
import de.robogo.fll.entity.RoundMode;
import de.robogo.fll.entity.Table;
import de.robogo.fll.entity.Team;
import de.robogo.fll.entity.TimeSlot;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public class FLLController {

	//TODO set in Importer!
	private static String eventName = "FLL Regio Hochtesthausen";

	private static final List<Team> teams = new ArrayList<>();

	private static final List<Table> tables = new ArrayList<>(Arrays.asList(new Table("1"), new Table("2"), new Table("3"), new Table("4")));

	private static final List<TimeSlot> timeSlots = new ArrayList<>();

	private static final ObjectProperty<RobotGameTimeSlot> activeSlot = new SimpleObjectProperty<>();

	public static List<Team> getTeams() {
		return teams;
	}

	public static List<TimeSlot> getTimeSlots() {
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

	public static void setTimeSlots(final List<TimeSlot> timeSlots) {
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

	public static List<RobotGameTimeSlot> getTimeSlotsByRoundMode(RoundMode roundMode) {
		List<RobotGameTimeSlot> retVal = new ArrayList<>();
		for (TimeSlot timeSlot : getTimeSlots()) {
			if (timeSlot instanceof RobotGameTimeSlot) {
				RobotGameTimeSlot rgts = (RobotGameTimeSlot) timeSlot;
				if (rgts.getRoundMode().equals(roundMode))
					retVal.add(rgts);
			}
		}
		return retVal;
	}

	public static List<JuryTimeSlot> getJuryTimeSlots() {
		List<JuryTimeSlot> retVal = new ArrayList<>();
		for (TimeSlot timeSlot : getTimeSlots()) {
			if (timeSlot instanceof JuryTimeSlot)
				retVal.add((JuryTimeSlot) timeSlot);
		}
		return retVal;
	}

	public static Map<LocalTime, List<JuryTimeSlot>> getJuryTimeSlotsGrouped() {
		return getJuryTimeSlots().stream().collect(Collectors.groupingBy(TimeSlot::getTime));
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

	public static RobotGameTimeSlot getActiveSlot() {
		return activeSlot.get();
	}

	public static ObjectProperty<RobotGameTimeSlot> getActiveSlotProperty() {
		return activeSlot;
	}

	public static void setActiveSlot(final RobotGameTimeSlot activeSlot) {
		FLLController.activeSlot.setValue(activeSlot);
	}
}
