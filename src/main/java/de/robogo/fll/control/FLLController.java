package de.robogo.fll.control;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

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

/**
 * This class saves all Information about the Tournament.
 * It provides multiple helper-Methods
 */
public class FLLController {

	//kann sich das ändern?
	public static final int ROBOT_GAME_SLOT_DURATION = 5;
	public static final int JURY_SLOT_DURATION = 15;
	public static final int LC_SLOT_DURATION = 30;

	private static String eventName = "FLL Regio Hochtesthausen";

	private static final ObservableList<Team> teams = FXCollections.observableArrayList();

	private static final ObservableList<Table> tables = FXCollections.observableArrayList();

	private static final ObservableList<TimeSlot> timeSlots = FXCollections.observableArrayList();

	private static final ObservableList<Jury> juries = FXCollections.observableArrayList();

	private static final ObjectProperty<LocalTime> activeTime = new SimpleObjectProperty<>();

	/**
	 * Returns all Teams. Changes to this List will be applied globally.
	 * @return team list
	 */
	@NonNull
	public static ObservableList<Team> getTeams() {
		return teams;
	}

	/**
	 * Returns all TimeSlots. Changes to this List will be applied globally
	 * @return timeSlot list
	 */
	@NonNull
	public static ObservableList<TimeSlot> getTimeSlots() {
		return timeSlots;
	}

	/**
	 * Returns all Tables. Changes to this List will be applied globally
	 * @return table list
	 */
	@NonNull
	public static ObservableList<Table> getTables() {
		return tables;
	}

	/**
	 * Returns all Juries. Changes to this List will be applied globally
	 * @return jury list
	 */
	@NonNull
	public static ObservableList<Jury> getJuries() {
		return juries;
	}

	/**
	 * Sets the new table list.
	 * This method first clears all other tables,
	 * it also adds a null-Table for User-Selection
	 * @param tables new tables
	 */
	public static void setTables(final List<Table> tables) {
		FLLController.tables.clear();
		if (tables != null && !tables.isEmpty()) {
			if (!tables.contains(null))
				FLLController.tables.add(null);
			FLLController.tables.addAll(tables);
		}
	}

	/**
	 * Sets the new team list.
	 * this method first clears all other teams,
	 * it also adds a null-Team for User-Selection
	 * @param teams new tables
	 */
	public static void setTeams(final List<Team> teams) {
		FLLController.teams.clear();
		if (teams != null && !teams.isEmpty()) {
			if (!teams.contains(null))
				FLLController.teams.add(null);
			FLLController.teams.addAll(teams);
		}
	}

	/**
	 * Sets the new timeSlot list.
	 * this method first clears all other timeSlots.
	 * there is NO null-Slot!
	 * @param timeSlots new timeSlots
	 */
	public static void setTimeSlots(final List<TimeSlot> timeSlots) {
		FLLController.timeSlots.clear();
		FLLController.timeSlots.addAll(timeSlots);
	}

	/**
	 * Sets the new jury list.
	 * this method first clears all other juries.
	 * there is NO null-jury!
	 * @param juries new juries
	 */
	public static void setJuries(final List<Jury> juries) {
		FLLController.juries.clear();
		FLLController.juries.addAll(juries);
	}

	/**
	 * Sets the new jury list.
	 * this method first clears all other juries.
	 * there is NO null-jury!
	 * @param juries new juries
	 * @see FLLController#addJuries(Jury...) (no clearing)
	 */
	public static void setJuries(Jury... juries) {
		FLLController.juries.clear();
		addJuries(juries);
	}

	/**
	 * Add new juries to the existing list without clearing the list.
	 * @param juries additional juries
	 * @see FLLController#setJuries(Jury...) (with clearing)
	 */
	public static void addJuries(Jury... juries) {
		FLLController.juries.addAll(Arrays.asList(juries));
	}

	/**
	 * Finds the Team-Object with the matching Name.
	 * @param name name to search for
	 * @return the first matching team, null if no team was found
	 */
	@Nullable
	public static Team getTeamByName(String name) {
		for (Team t : getTeams()) {
			if (t != null && t.getName().equals(name))
				return t;
		}
		return null;
	}

	/**
	 * Filters the TimeSlots by the given RoundMode.
	 * Changes to this list are not applied to the global data!
	 * @param roundMode to filter
	 * @return a new list containing all matching RobotGameTimeSlots.
	 * @see FLLController#getTimeSlots()
	 */
	@NonNull
	public static List<RobotGameTimeSlot> getTimeSlotsByRoundMode(RoundMode roundMode) {
		return getTimeSlots().stream()
				.filter(timeSlot -> timeSlot instanceof RobotGameTimeSlot)
				.map(timeSlot -> (RobotGameTimeSlot) timeSlot)
				.filter(timeSlot -> timeSlot.getRoundMode().equals(roundMode))
				.sorted(Comparator.comparing(TimeSlot::getTime))
				.collect(Collectors.toList());
	}

	/**
	 * Filters the timeslots by JuryTimeSlot subclass.
	 * Changes to this list are not applied to the global data!
	 * This list contains no pause slots.
	 * @return all JurySlots
	 * @see FLLController#getJurySlotsWithPause()
	 */
	@NonNull
	public static List<JuryTimeSlot> getJuryTimeSlots() {
		return getTimeSlots().stream()
				.filter(timeSlot -> timeSlot instanceof JuryTimeSlot)
				.map(timeSlot -> (JuryTimeSlot) timeSlot)
				.sorted(Comparator.comparing(TimeSlot::getTime))
				.collect(Collectors.toList());
	}

	/**
	 * Groups JuryTimeSlots by starting Time.
	 * the resulting Map is navigable with higherKey() and lowerKey().
	 * this map does not contain any pauses.
	 * Changes to this map are not applied to the global data!
	 * @return the grouped map
	 * @see FLLController#getJuryTimeSlotsWithPauseGrouped() (with pauses)
	 */
	@NonNull
	public static NavigableMap<LocalTime, List<JuryTimeSlot>> getJuryTimeSlotsGrouped() {
		return getTimeSlots().stream()
				.filter(timeSlot -> timeSlot instanceof JuryTimeSlot)
				.map(timeSlot -> (JuryTimeSlot) timeSlot)
				.sorted(Comparator.comparing(TimeSlot::getTime))
				.collect(Collectors.groupingBy(TimeSlot::getTime, () -> new TreeMap<>(Comparator.naturalOrder()), Collectors.toList()));
	}

	/**
	 * Filters the TimeSlots by JurySlot interface.
	 * The resulting List also contains the pause slots of all Juries.
	 * Changes to this list are not applied to the global data!
	 * @return the filtered list
	 * @see FLLController#getJuryTimeSlots()
	 */
	@NonNull
	public static List<JurySlot> getJurySlotsWithPause() {
		return getTimeSlots().stream()
				.filter(timeSlot -> timeSlot instanceof JurySlot)
				.map(timeSlot -> (JurySlot) timeSlot)
				.sorted(Comparator.comparing(JurySlot::getTime))
				.collect(Collectors.toList());
	}

	/**
	 * Groups JurySlots by starting Time.
	 * the resulting Map is navigable with higherKey() and lowerKey().
	 * this map contains common jury pauses.
	 * Changes to this map are not applied to the global data!
	 * @return the grouped map
	 * @see FLLController#getJuryTimeSlotsGrouped() () (without pauses)
	 */
	@NonNull
	public static NavigableMap<LocalTime, List<JurySlot>> getJuryTimeSlotsWithPauseGrouped() {
		return getTimeSlots().stream()
				.filter(timeSlot -> timeSlot instanceof JurySlot)
				.map(timeSlot -> (JurySlot) timeSlot)
				.sorted(Comparator.comparing(JurySlot::getTime))
				.collect(Collectors.groupingBy(JurySlot::getTime, () -> new TreeMap<>(Comparator.naturalOrder()), Collectors.toList()));
	}

	/**
	 * Gets the table with the provided Number using list indexes
	 * @param num number
	 * @return table at index
	 * @throws IndexOutOfBoundsException if the table does not exist
	 * @see FLLController#getTables()
	 */
	@NonNull
	public static Table getTableByNumber(int num) {
		return getTableByNumber(num, getTables());
	}

	/**
	 * Gets the table with the provided Number using list indexes
	 * <p>
	 * this method is used by the importer
	 *
	 * @param num number
	 * @param tables list to search in
	 * @return table at index
	 * @throws IndexOutOfBoundsException if the table does not exist
	 * @see FLLController#getTables()
	 */
	@NonNull
	public static Table getTableByNumber(int num, List<Table> tables) {
		if (tables.contains(null))
			return tables.get(num - 2);
		return getTables().get(num - 1);
	}

	/**
	 * Splits the jury identifier into type and num
	 * and finds the matching jury object.
	 * identifier format: shortName + number
	 * e.g F2 -> Research 2, TR1 -> TestRound 1
	 * @param identifier the formated identifier
	 * @return the matching jury object,
	 * null if not found or a invalid identifier was given
	 * @see JuryType#getShortName()
	 * @see FLLController#getJuryTypeByShortName(String)
	 * @see FLLController#getJury(JuryType, int)
	 */
	@Nullable
	public static Jury getJuryByIdentifier(String identifier) {
		return getJuryByIdentifier(identifier, getJuries());
	}

	/**
	 * Splits the jury identifier into type and num
	 * and finds the matching jury object.
	 * identifier format: shortName + number
	 * e.g F2 -> Research 2, TR1 -> TestRound 1
	 * <p>
	 * this method is used by the importer.
	 *
	 * @param identifier the formated identifier
	 * @param juries     list to search in
	 * @return the matching jury object,
	 * null if not found or a invalid identifier was given
	 * @see JuryType#getShortName()
	 * @see FLLController#getJuryTypeByShortName(String)
	 * @see FLLController#getJury(JuryType, int)
	 */
	@Nullable
	public static Jury getJuryByIdentifier(String identifier, List<Jury> juries) {
		String juryType = identifier.replaceAll("[0-9]", "");
		String juryNum = identifier.replaceAll("[a-zA-Z]", "");
		JuryType type = getJuryTypeByShortName(juryType);
		try {
			int num = Integer.parseInt(juryNum);
			return getJury(type, num, juries);
		} catch (NumberFormatException ignore) {
		}
		return null;
	}

	/**
	 * finds the juryType with the given short name
	 * using the german short names (e.g. F -> Research)
	 * @param shortName to search for
	 * @return the matching JuryType, null if not found
	 * @see JuryType#getShortName()
	 */
	@Nullable
	public static JuryType getJuryTypeByShortName(String shortName) {
		for (JuryType juryType : JuryType.values())
			if (juryType.getShortName().equals(shortName))
				return juryType;
		return null;
	}

	/**
	 * finds the jury with the matching type und number.
	 * @param type to search for
	 * @param num to search for
	 * @return the matching jury object
	 * null if no jury was found
	 */
	@Nullable
	public static Jury getJury(JuryType type, int num) {
		return getJury(type, num, getJuries());
	}

	/**
	 * finds the jury with the matching type und number.
	 * <p>
	 * this method is used by the importer.
	 *
	 * @param type   to search for
	 * @param num    to search for
	 * @param juries list to search in
	 * @return the matching jury object
	 * null if no jury was found
	 */
	@Nullable
	public static Jury getJury(JuryType type, int num, List<Jury> juries) {
		for (Jury jury : juries)
			if (jury.getJuryType().equals(type) && jury.getNum() == num)
				return jury;
		return null;
	}

	/**
	 * computes the maximum number of parallel jury groups of one juryType
	 * @return number of used jury groups
	 */
	public static int getMaxJuryNum() {
		int max = 0;
		for (Jury jury : getJuries()) {
			max = Math.max(max, jury.getNum());
		}
		return max;
	}

	/**
	 * this methods generates a list
	 * containing all slots assessed of the given jury.
	 * Changes to this list are not applied to the global data!
	 * this list does not contain any pauses-
	 * @param jury to filter
	 * @return the filtered list
	 * @see FLLController#getTimeSlotsByJuryWithPauses(Jury)
	 */
	public static List<JuryTimeSlot> getTimeSlotsByJury(Jury jury) {
		return getTimeSlots().stream()
				.filter(timeSlot -> timeSlot instanceof JuryTimeSlot)
				.map(timeSlot -> (JuryTimeSlot) timeSlot)
				.filter(timeSlot -> timeSlot.getJury().equals(jury))
				.sorted(Comparator.comparing(TimeSlot::getTime))
				.collect(Collectors.toList());
	}

	/**
	 * this methods generates a list
	 * containing all slots assessed of the given jury.
	 * if the jury has no new slot after the assessment is finished
	 * a new JuryPauseTimeSlot will be generated and added to the list.
	 * This list also contains common pauses.
	 * Changes to this list are not applied to the global data!
	 * @param jury to filter
	 * @return the filtered list
	 * @see FLLController#getTimeSlotsByJury(Jury)
	 */
	public static List<JurySlot> getTimeSlotsByJuryWithPauses(Jury jury) {
		if (jury == null)
			return Collections.emptyList();
		int duration = jury.getJuryType() == JuryType.LiveChallenge ? LC_SLOT_DURATION : JURY_SLOT_DURATION;
		List<JurySlot> result = new ArrayList<>();
		List<JuryTimeSlot> juryTimeSlots = getTimeSlotsByJury(jury);
		for (int i = 0; i < juryTimeSlots.size() - 1; i++) {
			JuryTimeSlot slot = juryTimeSlots.get(i);
			result.add(slot);
			LocalTime nextTime = slot.getTime().plusMinutes(duration);
			if (juryTimeSlots.get(i + 1).getTime().isAfter(nextTime)) {
				result.add(new JuryPauseTimeSlot(nextTime));
			}
		}
		return result;
	}

	/**
	 * returns the name of this event
	 * e.g "FLL Regionalwettbewerb München"
	 * @return the event name
	 */
	@NonNull
	public static String getEventName() {
		return eventName;
	}

	/**
	 * sets the new event name
	 * @param eventName new event name
	 */
	public static void setEventName(final String eventName) {
		FLLController.eventName = eventName;
	}

	/**
	 * finds the first TimeSlot in the list with the matching starting time
	 * this is commonly used in the RobotGame context
	 * @return the active TimeSlot
	 */
	@NonNull
	public static Optional<TimeSlot> getActiveSlot() {
		return getTimeSlots().stream()
				.filter(timeSlot -> timeSlot.getTime().equals(getActiveTime()))
				.findFirst();
	}

	/**
	 * finds all TimeSlots in the list with the matching starting time
	 * this is commonly used in the JurySlot context
	 * @return the active TimeSlots
	 */
	@NonNull
	public static List<TimeSlot> getActiveSlots() {
		return getTimeSlots().stream()
				.filter(timeSlot -> timeSlot.getTime().equals(getActiveTime()))
				.collect(Collectors.toList());
	}

	/**
	 * returns the active starting time
	 * @return active time
	 */
	@Nullable
	public static LocalTime getActiveTime() {
		return activeTime.get();
	}

	/**
	 * returns the active starting time as object property
	 * @return active time property
	 */
	@NonNull
	public static ObjectProperty<LocalTime> getActiveTimeProperty() {
		return activeTime;
	}

	/**
	 * sets the active starting time to the starting time of the provided TimeSlot
	 * @param activeTime TimeSlot with new active Time
	 */
	public static void setActiveTime(final TimeSlot activeTime) {
		FLLController.activeTime.setValue(activeTime == null ? null : activeTime.getTime());
	}

	/**
	 * sets the active time to the provided time
	 * @param localTime new active time
	 */
	public static void setActiveTime(final LocalTime localTime) {
		FLLController.activeTime.setValue(localTime);
	}
}
