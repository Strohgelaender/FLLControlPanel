package de.robogo.fll.screens.websocket;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/timer")
public class TimerController {

	private static final long GAME_TIME = (2 * 60 + 30) * 1000;
	private final SimpMessagingTemplate template;

	public TimerController(final SimpMessagingTemplate template) {
		this.template = template;
	}

	@GetMapping("/colorSettings")
	public Boolean getColorSettings() {
		//TODO value + correct Datatype
		return true;
	}

	public void startGame() {
		start(GAME_TIME, true);
	}

	public void resetGame() {
		reset(GAME_TIME, true);
	}

	public void start(long time, boolean game) {
		sendTimerMessage(new TimerMessage(time, game, true));
	}

	public void reset(long time, boolean game) {
		sendTimerMessage(new TimerMessage(time, game, false));
	}

	private void sendTimerMessage(TimerMessage timerMessage) {
		template.convertAndSend("/topic/timer", timerMessage);
	}

}
