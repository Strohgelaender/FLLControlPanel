package de.robogo.fll.screens;

import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import de.robogo.fll.control.FLLController;
import de.robogo.fll.entity.RoundMode;
import de.robogo.fll.entity.timeslot.RobotGameTimeSlot;
import de.robogo.fll.entity.timeslot.TimeSlot;

@Controller
public class ScreenWebController {

	@GetMapping("/scoreboard")
	public String scoreboard() {
		return "scoreboard";
	}

	@GetMapping("/timetable")
	public String timetable(@RequestParam(name = "round", required = false, defaultValue = "current") String round, Model model) {
		model.addAttribute("eventName", FLLController.getEventName());
		RoundMode roundMode = null;
		if (round.equalsIgnoreCase("current")) {
			Optional<TimeSlot> activeSlot = FLLController.getActiveSlot();
			if (activeSlot.isPresent() && activeSlot.get() instanceof RobotGameTimeSlot)
				roundMode = ((RobotGameTimeSlot) activeSlot.get()).getRoundMode();
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
			model.addAttribute("timeslots", FLLController.getTimeSlotsByRoundMode(roundMode));
		return "timetable";
	}

	@GetMapping("/juryMatrix")
	public String juryMatrix() {
		return jury();
	}

	@GetMapping("/jury")
	public String jury() {
		return "juryMatrix";
	}

	@GetMapping("/room")
	public String room() {
		return "room";
	}

}
