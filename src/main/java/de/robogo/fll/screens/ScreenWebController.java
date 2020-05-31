package de.robogo.fll.screens;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import de.robogo.fll.control.FLLController;
import de.robogo.fll.entity.RoundMode;

@Controller
public class ScreenWebController {

	@GetMapping("/scoreboard")
	public String scoreboard(Model model) {
		model.addAttribute("eventName", FLLController.getEventName());
		model.addAttribute("teams", FLLController.getTeams());
		return "scoreboard";
	}

	//TODO TestRound / Jury Timetable
	@GetMapping("/timetable")
	public String timetable(@RequestParam(name = "round", required = false, defaultValue = "current") String round, Model model) {
		model.addAttribute("eventName", FLLController.getEventName());
		if (round.equalsIgnoreCase("current")) {
			if (FLLController.getActiveSlot() != null)
				model.addAttribute("timeslots", FLLController.getTimeSlotsByRoundMode(FLLController.getActiveSlot().getRoundMode()));
		} else {
			try {
				RoundMode roundMode = RoundMode.valueOf(round);
				model.addAttribute("timeslots", FLLController.getTimeSlotsByRoundMode(roundMode));
			} catch (IllegalArgumentException ignored) {
			}
		}
		return "timetable";
	}

}
