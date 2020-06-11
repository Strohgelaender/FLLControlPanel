package de.robogo.fll.control;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import de.robogo.fll.entity.Jury;
import de.robogo.fll.entity.Jury.JuryType;
import de.robogo.fll.entity.RoundMode;
import de.robogo.fll.entity.Table;
import de.robogo.fll.entity.Team;
import de.robogo.fll.entity.timeslot.JuryPauseTimeSlot;
import de.robogo.fll.entity.timeslot.JurySlot;
import de.robogo.fll.entity.timeslot.JuryTimeSlot;
import de.robogo.fll.entity.timeslot.RobotGameTimeSlot;
import de.robogo.fll.entity.timeslot.TimeSlot;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class FLLController {

	//kann sich das ändern?
	public static final int ROBOT_GAME_SLOT_DURATION = 5;
	public static final int JURY_SLOT_DURATION = 15;
	public static final int LC_SLOT_DURATION = 30;

	private static String eventName = "FLL Regio Hochtesthausen";

	private static final ObservableList<Team> teams = FXCollections.observableArrayList();

	private static final ObservableList<Table> tables = FXCollections.observableArrayList(Arrays.asList(new Table("1"), new Table("2"), new Table("3"), new Table("4")));

	private static final ObservableList<TimeSlot> timeSlots = FXCollections.observableArrayList();

	private static final ObservableList<Jury> juries = FXCollections.observableArrayList();

	private static final ObjectProperty<TimeSlot> activeSlot = new SimpleObjectProperty<>(); //TODO chage to DayTimeProperty

	public static ObservableList<Team> getTeams() {
		return teams;
	}

	public static ObservableList<TimeSlot> getTimeSlots() {
		return timeSlots;
	}

	public static ObservableList<Table> getTables() {
		return tables;
	}

	public static ObservableList<Jury> getJuries() {
		return juries;
	}

	public static void setTables(final List<Table> tables) {
		FLLController.tables.clear();
		if (tables != null && !tables.isEmpty()) {
			if (!tables.contains(null))
				FLLController.tables.add(null);
			FLLController.tables.addAll(tables);
		}
	}

	public static void setTeams(final List<Team> teams) {
		FLLController.teams.clear();
		if (teams != null && !teams.isEmpty()) {
			if (!teams.contains(null))
				FLLController.teams.add(null);
			FLLController.teams.addAll(teams);
		}
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
			if (t != null && t.getName().equals(name))
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
		return getTimeSlots().stream()
				.filter(timeSlot -> timeSlot instanceof JuryTimeSlot)
				.map(timeSlot -> (JuryTimeSlot) timeSlot)
				.sorted(Comparator.comparing(TimeSlot::getTime))
				.collect(Collectors.groupingBy(TimeSlot::getTime, () -> new TreeMap<>(Comparator.naturalOrder()), Collectors.toList()));
	}

	public static List<JurySlot> getJurySlotsWithPause() {
		return getTimeSlots().stream()
				.filter(timeSlot -> timeSlot instanceof JurySlot)
				.map(timeSlot -> (JurySlot) timeSlot)
				.sorted(Comparator.comparing(JurySlot::getTime))
				.collect(Collectors.toList());
	}

	public static NavigableMap<LocalTime, List<JurySlot>> getJuryTimeSlotsWithPauseGrouped() {
		return getTimeSlots().stream()
				.filter(timeSlot -> timeSlot instanceof JurySlot)
				.map(timeSlot -> (JurySlot) timeSlot)
				.sorted(Comparator.comparing(JurySlot::getTime))
				.collect(Collectors.groupingBy(JurySlot::getTime, () -> new TreeMap<>(Comparator.naturalOrder()), Collectors.toList()));
	}

	public static Table getTableByNumber(int num) {
		if (tables.contains(null))
			return tables.get(num - 2);
		return getTables().get(num - 1);
	}

	public static Jury getJuryByIdentifier(String identifier) {
		String juryType = identifier.replaceAll("[0-9]", "");
		String juryNum = identifier.replaceAll("[a-zA-Z]", "");
		JuryType type = getJuryTypeByShortName(juryType);
		try {
			int num = Integer.parseInt(juryNum);
			return getJury(type, num);
		} catch (NumberFormatException ignore) {
		}
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

	public static int getMaxJuryNum() {
		int max = 0;
		for (Jury jury : getJuries()) {
			max = Math.max(max, jury.getNum());
		}
		return max;
	}

	public static List<JuryTimeSlot> getTimeSlotsByJury(Jury jury) {
		return getTimeSlots().stream()
				.filter(timeSlot -> timeSlot instanceof JuryTimeSlot)
				.map(timeSlot -> (JuryTimeSlot) timeSlot)
				.filter(timeSlot -> timeSlot.getJury().equals(jury))
				.sorted(Comparator.comparing(TimeSlot::getTime))
				.collect(Collectors.toList());
	}

	public static List<JurySlot> getJuryTimeSlotsByJuryWithPauses(Jury jury) {
		if (jury == null)
			return Collections.emptyList();
		int duration = jury.getJuryType() == JuryType.LiveChallenge ? LC_SLOT_DURATION : JURY_SLOT_DURATION;
		List<JurySlot> result = new ArrayList<>();
		List<JuryTimeSlot> juryTimeSlots = getTimeSlotsByJury(jury);
		for (int i = 0; i < juryTimeSlots.size() - 1; i++) {
			JuryTimeSlot slot = juryTimeSlots.get(i);
			result.add(slot);
			LocalTime nextTime = slot.getTime().plusMinutes(duration);
			if (juryTimeSlots.get(i+1).getTime().isAfter(nextTime)) {
				result.add(new JuryPauseTimeSlot(nextTime));
			}
		}
		return result;
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
