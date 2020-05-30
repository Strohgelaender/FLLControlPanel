package de.robogo.fll.screens;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import de.robogo.fll.control.FLLController;

@Controller
public class ScreenWebController {

	@GetMapping("/scoreboard")
	public String scoreboard(Model model) {
		model.addAttribute("eventName", FLLController.getEventName());
		model.addAttribute("teams", FLLController.getTeams());
		return "scoreboard";
	}

	@GetMapping("/timetable")
	public String timetable(Model model) {
		model.addAttribute("eventName", FLLController.getEventName());
		model.addAttribute("timeslots", FLLController.getTimeSlots());
		return "timetable";
	}

}
