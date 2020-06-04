package de.robogo.fll.control;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import de.robogo.fll.entity.Jury;
import de.robogo.fll.entity.Jury.JuryType;
import de.robogo.fll.entity.JuryTimeSlot;
import de.robogo.fll.entity.RobotGameTimeSlot;
import de.robogo.fll.entity.RoundMode;
import de.robogo.fll.entity.Table;
import de.robogo.fll.entity.Team;
import de.robogo.fll.entity.TimeSlot;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public class FLLController {

	private static String eventName = "FLL Regio Hochtesthausen";

	private static final List<Team> teams = new ArrayList<>();

	private static final List<Table> tables = new ArrayList<>(Arrays.asList(new Table("1"), new Table("2"), new Table("3"), new Table("4")));

	private static final List<TimeSlot> timeSlots = new ArrayList<>();

	private static final List<Jury> juries = new ArrayList<>();

	private static final ObjectProperty<TimeSlot> activeSlot = new SimpleObjectProperty<>();

	public static List<Team> getTeams() {
		return teams;
	}

	public static List<TimeSlot> getTimeSlots() {
		return timeSlots;
	}

	public static List<Table> getTables() {
		return tables;
	}

	public static List<Jury> getJuries() {
		return juries;
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

	public static void setJuries(final List<Jury> juries) {
		FLLController.juries.clear();
		FLLController.juries.addAll(juries);
	}

	public static void setJuries(Jury... juries) {
		FLLController.juries.clear();
		addJuries(juries);
	}

	public static void addJuries(Jury... juries) {
		FLLController.juries.addAll(Arrays.asList(juries));
	}

	public static Team getTeamByName(String name) {
		for (Team t : getTeams()) {
			if (t.getName().equals(name))
				return t;
		}
		return null;
	}

	public static List<RobotGameTimeSlot> getTimeSlotsByRoundMode(RoundMode roundMode) {
		return getTimeSlots().stream()
				.filter(timeSlot -> timeSlot instanceof RobotGameTimeSlot)
				.map(timeSlot -> (RobotGameTimeSlot) timeSlot)
				.filter(timeSlot -> timeSlot.getRoundMode().equals(roundMode))
				.sorted(Comparator.comparing(TimeSlot::getTime))
				.collect(Collectors.toList());
	}

	public static List<JuryTimeSlot> getJuryTimeSlots() {
		return getTimeSlots().stream()
				.filter(timeSlot -> timeSlot instanceof JuryTimeSlot)
				.map(timeSlot -> (JuryTimeSlot) timeSlot)
				.sorted(Comparator.comparing(TimeSlot::getTime))
				.collect(Collectors.toList());
	}

	public static NavigableMap<LocalTime, List<JuryTimeSlot>> getJuryTimeSlotsGrouped() {
		return getJuryTimeSlots().stream().sorted(Comparator.comparing(TimeSlot::getTime)).collect(Collectors.groupingBy(TimeSlot::getTime, () -> new TreeMap<>(Comparator.naturalOrder()), Collectors.toList()));
	}

	public static Table getTableByNumber(int num) {
		return getTables().get(num - 1);
	}

	public static Jury getJuryByIdentifier(String identifier) {
		String juryType = identifier.replaceAll("[0-9]", "");
		String juryNum = identifier.replaceAll("[a-zA-Z]", "");
		JuryType type = getJuryTypeByShortName(juryType);
		try {
			int num = Integer.parseInt(juryNum);
			return getJury(type, num);
		} catch (NumberFormatException ignore) {}
		return null;
	}

	public static JuryType getJuryTypeByShortName(String shortName) {
		for (JuryType juryType : JuryType.values())
			if (juryType.getShortName().equals(shortName))
				return juryType;
		return null;
	}

	public static Jury getJury(JuryType type, int num) {
		for (Jury jury : getJuries())
			if (jury.getJuryType().equals(type) && jury.getNum() == num)
				return jury;
		return null;
	}

	public static List<JuryTimeSlot> getTimeSlotsByJury(Jury jury) {
		return getTimeSlots().stream()
				.filter(timeSlot -> timeSlot instanceof JuryTimeSlot)
				.map(timeSlot -> (JuryTimeSlot) timeSlot)
				.filter(timeSlot -> timeSlot.getJury().equals(jury))
				.sorted(Comparator.comparing(TimeSlot::getTime))
				.collect(Collectors.toList());
	}

	public static String getEventName() {
		return eventName;
	}

	public static void setEventName(final String eventName) {
		FLLController.eventName = eventName;
	}

	public static TimeSlot getActiveSlot() {
		return activeSlot.get();
	}

	public static ObjectProperty<TimeSlot> getActiveSlotProperty() {
		return activeSlot;
	}

	public static void setActiveSlot(final TimeSlot activeSlot) {
		FLLController.activeSlot.setValue(activeSlot);
	}
}
