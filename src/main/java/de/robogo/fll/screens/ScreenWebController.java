package de.robogo.fll.screens;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ScreenWebController {

	@GetMapping("/scoreboard")
	public String scoreboard() {
		return "scoreboard";
	}

	@GetMapping("/timetable")
	public String timetable() {
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

	@GetMapping("/timer")
	public String timer(Model model) {
		model.addAttribute("timer", true);
		return "timer";
	}

	@GetMapping("/clock")
	public String clock(Model model) {
		//use the same base for Timer an clock
		model.addAttribute("timer", false);
		return "timer";
	}

}
