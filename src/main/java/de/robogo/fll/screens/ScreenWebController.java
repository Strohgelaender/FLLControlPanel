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
		RoundMode roundMode = null;
		if (round.equalsIgnoreCase("current")) {
			if (FLLController.getActiveSlot() != null)
				roundMode = FLLController.getActiveSlot().getRoundMode();
		} else {
			try {
				roundMode = RoundMode.valueOf(round);
			} catch (IllegalArgumentException ignored) {
				try {
					int i = Integer.parseInt(round);
					roundMode = RoundMode.values()[i];
				} catch (Exception ignored2) {
				}
			}
		}
		if (roundMode != null)
			model.addAttribute("timeslots", FLLController.getTimeSlotsByRoundMode(roundMode));
		return "timetable";
	}

}
