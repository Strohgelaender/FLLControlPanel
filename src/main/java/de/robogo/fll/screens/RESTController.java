package de.robogo.fll.screens;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.robogo.fll.control.FLLController;
import de.robogo.fll.entity.Jury;
import de.robogo.fll.entity.RoundMode;
import de.robogo.fll.entity.Team;
import de.robogo.fll.entity.timeslot.JurySlot;
import de.robogo.fll.entity.timeslot.JuryTimeSlot;
import de.robogo.fll.entity.timeslot.PauseTimeSlot;
import de.robogo.fll.entity.timeslot.RobotGameSlot;
import de.robogo.fll.entity.timeslot.TimeSlot;

/**
 * This controller implements the REST-API
 * it returns the requested information in JSON.
 */
@RestController
@RequestMapping("/rest")
public class RESTController {

	//TODO Filter (?) hinzufügen und zu JSON mit status-Wert ändern

	//TODO die locahost-Refernezen aus den Clients entfernen (das ist Dev-Only!)

	@GetMapping("/slots")
	public List<TimeSlot> allSlots() {
		return FLLController.getTimeSlots();
	}

	@GetMapping("/name")
	public String name() {
		return eventName();
	}

	@GetMapping("/eventName")
	public String eventName() {
		return FLLController.getEventName();
	}

	@GetMapping("/score")
	public List<Team> score() {
		return FLLController.getTeams().filtered(Objects::nonNull);
	}

	@GetMapping("/scores")
	public List<Team> scores() {
		return score();
	}

	@GetMapping("/teams")
	public List<Team> teams() {
		return score();
	}

	@GetMapping("/jury/{jury}")
	public Jury jury(@PathVariable String jury) {
		return FLLController.getJuryByIdentifier(jury);
	}

	@GetMapping("/juries")
	public List<Jury> juries() {
		return FLLController.getJuries();
	}

	@GetMapping("/timetable")
	public List<RobotGameSlot> timetable(@RequestParam(value = "round", required = false, defaultValue = "current") String round) {
		RoundMode roundMode = null;
		if (round.equalsIgnoreCase("current")) {
			Optional<TimeSlot> activeSlot = FLLController.getActiveSlot();
			if (activeSlot.isPresent() && activeSlot.get() instanceof RobotGameSlot)
				roundMode = ((RobotGameSlot) activeSlot.get()).getRoundMode();
		} else {
			try {
				roundMode = RoundMode.valueOf(round);
			} catch (IllegalArgumentException ignored) {
				try {
					int i = Integer.parseInt(round) - 1;
					roundMode = RoundMode.values()[i];
				} catch (Exception ignored2) {
				}
			}
		}
		if (roundMode != null)
			return FLLController.getTimeSlotsByRoundModeWithPauses(roundMode);
		return Collections.emptyList();
	}

	@GetMapping("/juryTimeSlots")
	public List<JuryTimeSlot> juryTimeSlots() {
		return FLLController.getJuryTimeSlots();
	}

	@GetMapping("/juryTimeSlotsGrouped")
	public SortedMap<LocalTime, List<JuryTimeSlot>> juryTimeSlotsGrouped() {
		return FLLController.getJuryTimeSlotsGrouped();
	}

	@GetMapping("/juryG")
	public SortedMap<LocalTime, List<JuryTimeSlot>> juryG() {
		return juryTimeSlotsGrouped();
	}

	@GetMapping("/juryTimeSlotsGroupedWithPause")
	public SortedMap<LocalTime, List<JurySlot>> juryTimeSlotsGroupedWithPause() {
		return FLLController.getJuryTimeSlotsWithPauseGrouped();
	}

	@GetMapping("/juryTimeSlotsWithPauseGrouped")
	public SortedMap<LocalTime, List<JurySlot>> juryTimeSlotsWithPauseGrouped() {
		return juryTimeSlotsGroupedWithPause();
	}

	@GetMapping("juryGP")
	public SortedMap<LocalTime, List<JurySlot>> juryGP() {
		return juryTimeSlotsGroupedWithPause();
	}

	@GetMapping("/room/{room}")
	public List<JuryTimeSlot> room(@PathVariable String room) {
		Jury jury = FLLController.getJuryByIdentifier(room);
		if (jury != null)
			return FLLController.getTimeSlotsByJury(jury);
		return Collections.emptyList();
	}

	@GetMapping("/room")
	public List<JuryTimeSlot> rooms(@RequestParam(name = "room") String room) {
		return room(room);
	}

	@GetMapping("/roomP/{room}")
	private List<JurySlot> roomP(@PathVariable String room) {
		Jury jury = FLLController.getJuryByIdentifier(room);
		if (jury != null)
			return FLLController.getTimeSlotsByJuryWithPauses(jury);
		return Collections.emptyList();
	}

	@GetMapping("/roomP")
	public List<JurySlot> roomsP(@RequestParam(name = "room") String room) {
		return roomP(room);
	}

	@GetMapping("/shortName/{juryType}")
	public String shortName(@PathVariable String juryType) {
		return Jury.JuryType.valueOf(juryType).getShortName();
	}

	@GetMapping("/longName/{juryType}")
	public String longName(@PathVariable String juryType) {
		return Jury.JuryType.valueOf(juryType).getLongName();
	}

	@GetMapping("/juryMatrix")
	public SortedMap<LocalTime, String[]> juryMatrix() {
		List<Team> teams = FLLController.getTeams().filtered(Objects::nonNull);
		Map<LocalTime, List<JurySlot>> juryGrouped = FLLController.getJuryTimeSlotsWithPauseGrouped();
		SortedMap<LocalTime, String[]> juryExport = new TreeMap<>(Comparator.naturalOrder());
		for (List<JurySlot> slotList : juryGrouped.values()) {
			if (slotList.isEmpty())
				continue;
			if (slotList.get(0) instanceof PauseTimeSlot)
				juryExport.put(slotList.get(0).getTime(), null);
			else {
				String[] strings = new String[teams.size()];
				Arrays.fill(strings, "");
				for (JurySlot timeSlot : slotList) {
					if (timeSlot instanceof JuryTimeSlot)
						strings[((JuryTimeSlot) timeSlot).getTeam().getId() - 1] = ((JuryTimeSlot) timeSlot).getJury().getJuryType().getShortName() + ((JuryTimeSlot) timeSlot).getJury().getNum();
				}
				juryExport.put(slotList.get(0).getTime(), strings);
			}
		}
		return juryExport;
	}
}
