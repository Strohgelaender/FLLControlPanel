package de.robogo.fll.screens;

import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.SortedMap;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.robogo.fll.control.FLLController;
import de.robogo.fll.entity.Jury;
import de.robogo.fll.entity.timeslot.JuryTimeSlot;
import de.robogo.fll.entity.Team;
import de.robogo.fll.entity.timeslot.TimeSlot;

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
}
