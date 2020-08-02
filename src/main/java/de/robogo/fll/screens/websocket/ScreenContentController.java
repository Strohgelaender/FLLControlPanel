package de.robogo.fll.screens.websocket;

import static de.robogo.fll.screens.websocket.ContentMessage.RefreshConfig;
import static de.robogo.fll.screens.websocket.ContentMessage.RefreshData;
import static de.robogo.fll.screens.websocket.ContentMessage.ShowBye;
import static de.robogo.fll.screens.websocket.ContentMessage.ShowNormal;
import static de.robogo.fll.screens.websocket.ContentMessage.ShowWelcome;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ScreenContentController {

	private static final String TOPIC = "/topic/content";

	private final SimpMessagingTemplate template;

	private ContentMessage initMessage = ShowNormal;

	public ScreenContentController(final SimpMessagingTemplate template) {
		this.template = template;
	}

	@MessageMapping("/content")
	@SendTo(TOPIC)
	public ContentMessage initScreenContent() {
		return initMessage;
	}

	public void showWelcome() {
		sendMessage(ShowWelcome);
		initMessage = ShowWelcome;
	}

	public void showNormal() {
		sendMessage(ShowNormal);
		initMessage = ShowNormal;
	}

	public void showBye() {
		sendMessage(ShowBye);
		initMessage = ShowBye;
	}

	public void refreshData() {
		sendMessage(RefreshData);
	}

	public void refreshConfig() {
		sendMessage(RefreshConfig);
	}

	private void sendMessage(ContentMessage message) {
		template.convertAndSend(TOPIC, message);
	}
}
