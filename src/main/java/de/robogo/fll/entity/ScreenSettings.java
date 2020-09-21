package de.robogo.fll.entity;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.commons.compress.utils.IOUtils;
import org.springframework.http.MediaType;

import de.robogo.fll.control.FLLController;
import de.robogo.fll.io.RoboGoIO;
import javafx.scene.paint.Color;

public class ScreenSettings {

	private static final Map<String, ScreenSettings> screenSettings = new HashMap<>();
	public static final ScreenSettings GLOBAL_SETTINGS = new ScreenSettings();

	private byte[] backgroundImage;
	private MediaType mediaType;

	private String fontFamily;
	private int fontSize = -1;
	private String fontStyle;
	private Color fontColor;

	private String h1;
	private String h2;

	//internal CSS, non-customizable by user
	private String internalSpecialCSS;
	//external CSS, user-input
	private String externalSpecialCSS;

	private final Map<String, Callable<String>> replacements = new HashMap<>();

	//set Default values
	static {
		try {
			InputStream stream = ScreenSettings.class.getClassLoader().getResourceAsStream("GameChangers.jpg");
			GLOBAL_SETTINGS.setBackgroundImage(IOUtils.toByteArray(stream));
			GLOBAL_SETTINGS.setMediaType(MediaType.IMAGE_PNG);
			GLOBAL_SETTINGS.setFontFamily("Segoe UI");
			GLOBAL_SETTINGS.setFontSize(25);
			GLOBAL_SETTINGS.setFontStyle("normal");
			GLOBAL_SETTINGS.setFontColor(Color.WHITE);
			GLOBAL_SETTINGS.setH1("{eventName}");
			GLOBAL_SETTINGS.setH2("");

			screenSettings.put("timetable", new ScreenSettings());

			ScreenSettings room = new JuryScreenSettings();
			room.setH2("{longName} {num} | {room}");
			screenSettings.put("room", room);

			ScreenSettings juryMatrix = new ScreenSettings();
			juryMatrix.setH2("Zeitplan Vormittag");
			screenSettings.put("juryMatrix", juryMatrix);

			ScreenSettings timer = new ScreenSettings();
			String timerCSS = RoboGoIO.readResourceFile("static/css/timer.css");
			timer.setInternalSpecialCSS(timerCSS);
			timer.setFontFamily("Roboto");
			timer.setH1("");
			timer.setH2("");
			screenSettings.put("timer", timer);
			//TODO make timer settings user-chageable (color, shadow, etc.)

			ScreenSettings clock = new ScreenSettings();
			clock.setInternalSpecialCSS(timerCSS);
			clock.setFontFamily("Roboto");
			clock.setH1("");
			clock.setH2("");
			screenSettings.put("clock", clock);

			String welcomeCSS = "#welcomeText { margin: 0;\n" +
					"width: 100%;\n" +
					"position: absolute;\n" +
					"top: 30%;\n" +
					"left: 50%;\n" +
					"-ms-transform: translate(-50%, -50%);\n" +
					"transform: translate(-50%, -50%); }";

			ScreenSettings welcome = new ScreenSettings();
			welcome.setH1("Herzlich Willkommen");
			welcome.setH2("zum {eventName}");
			welcome.setFontSize(40);
			welcome.setExternalSpecialCSS(welcomeCSS);
			screenSettings.put("welcome", welcome);

			ScreenSettings bye = new ScreenSettings();
			bye.setH1("Servus");
			bye.setH2("Bis zum nächsten Jahr!");
			bye.setFontSize(40);
			bye.setExternalSpecialCSS(welcomeCSS);
			screenSettings.put("bye", bye);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static ScreenSettings getScreenSettingsByScreenName(String screenName) {
		return screenSettings.getOrDefault(screenName, GLOBAL_SETTINGS);
	}

	public ScreenSettings() {
		replacements.put("eventName", FLLController::getEventName);
	}

	public Set<String> getReplacements() {
		return replacements.keySet();
	}

	public String applyReplacement(String replacement) {
		try {
			if (replacements.containsKey(replacement))
				return replacements.get(replacement).call();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	public byte[] getBackgroundImage() {
		return backgroundImage;
	}

	public void setBackgroundImage(final byte[] backgroundImage) {
		this.backgroundImage = backgroundImage;
	}

	public MediaType getMediaType() {
		return mediaType;
	}

	public void setMediaType(final MediaType mediaType) {
		this.mediaType = mediaType;
	}

	public String getFontFamily() {
		return fontFamily;
	}

	public void setFontFamily(final String fontFamily) {
		this.fontFamily = fontFamily;
	}

	public int getFontSize() {
		return fontSize;
	}

	public void setFontSize(final int fontSize) {
		this.fontSize = fontSize;
	}

	public String getFontStyle() {
		return fontStyle;
	}

	public void setFontStyle(final String fontStyle) {
		this.fontStyle = fontStyle;
	}

	public Color getFontColor() {
		return fontColor;
	}

	public void setFontColor(final Color fontColor) {
		this.fontColor = fontColor;
	}

	public String getExternalSpecialCSS() {
		return externalSpecialCSS;
	}

	public void setExternalSpecialCSS(final String externalSpecialCSS) {
		this.externalSpecialCSS = externalSpecialCSS;
	}

	public String getInternalSpecialCSS() {
		return internalSpecialCSS;
	}

	public void setInternalSpecialCSS(final String internalSpecialCSS) {
		this.internalSpecialCSS = internalSpecialCSS;
	}

	public String getH1() {
		return h1;
	}

	public void setH1(final String h1) {
		this.h1 = h1;
	}

	public String getH2() {
		return h2;
	}

	public void setH2(final String h2) {
		this.h2 = h2;
	}
}
