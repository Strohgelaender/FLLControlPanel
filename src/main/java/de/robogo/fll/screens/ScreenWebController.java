package de.robogo.fll.screens;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import de.robogo.fll.control.FLLController;
import de.robogo.fll.entity.JuryTimeSlot;
import de.robogo.fll.entity.RoundMode;
import de.robogo.fll.entity.Team;

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

	@GetMapping("/juryMatrix")
	public String juryMatrix(Model model) {
		return jury(model);
	}

	@GetMapping("/jury")
	public String jury(Model model) {
		model.addAttribute("eventName", FLLController.getEventName());
		List<Team> teams = FLLController.getTeams();
		model.addAttribute("teams", teams);
		Map<LocalTime, List<JuryTimeSlot>> juryGrooped =  FLLController.getJuryTimeSlotsGrouped();
		SortedMap<LocalTime, String[]> juryExport = new TreeMap<>(Comparator.naturalOrder());
		for (List<JuryTimeSlot> slotList : juryGrooped.values()) {
			String[] strings = new String[teams.size()];
			Arrays.fill(strings, "");
			for (JuryTimeSlot timeSlot : slotList) {
				strings[timeSlot.getTeam().getId() - 1] = timeSlot.getJuryType().getText() + timeSlot.getJuryNumber();
			}
			juryExport.put(slotList.get(0).getTime(), strings);
		}
		model.addAttribute("slots", juryExport);
		return "juryMatrix";
	}

}
