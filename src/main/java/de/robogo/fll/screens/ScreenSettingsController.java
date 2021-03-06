package de.robogo.fll.screens;

import static de.robogo.fll.entity.ScreenSettings.GLOBAL_SETTINGS;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import de.robogo.fll.control.FLLController;
import de.robogo.fll.entity.Jury;
import de.robogo.fll.entity.JuryScreenSettings;
import de.robogo.fll.entity.ScreenSettings;
import javafx.scene.paint.Color;

@Controller
@RequestMapping("/screens")
public class ScreenSettingsController {

	@GetMapping("/image")
	public ResponseEntity<byte[]> getImage(@RequestParam(name = "screen") String screen) {
		ScreenSettings screenSettings = ScreenSettings.getScreenSettingsByScreenName(screen);
		if (screenSettings == null)
			return null;

		byte[] media = screenSettings.getBackgroundImage();
		if (media == null)
			media = GLOBAL_SETTINGS.getBackgroundImage();

		HttpHeaders headers = new HttpHeaders();
		headers.setCacheControl(CacheControl.noCache().getHeaderValue());
		headers.setContentType(screenSettings.getMediaType());

		return new ResponseEntity<>(media, headers, HttpStatus.OK);
	}

	@GetMapping("/style")
	public ResponseEntity<String> generateCSS(@RequestParam(name = "screen") String screen) {
		ScreenSettings screenSettings = ScreenSettings.getScreenSettingsByScreenName(screen);

		String font = getOrDefault(screenSettings.getFontFamily(), GLOBAL_SETTINGS.getFontFamily());
		int fontSize = getOrDefault(screenSettings.getFontSize(), GLOBAL_SETTINGS.getFontSize());
		String fontStyle = getOrDefault(screenSettings.getFontStyle(), GLOBAL_SETTINGS.getFontStyle());
		Color fontColor = getOrDefault(screenSettings.getFontColor(), GLOBAL_SETTINGS.getFontColor());

		String css = "";

		css += "html { \n" +
				"  background: url(/screens/image?screen=" + screen + ") no-repeat center center fixed; \n" +
				"  -webkit-background-size: cover;\n" +
				"  -moz-background-size: cover;\n" +
				"  -o-background-size: cover;\n" +
				"  background-size: cover;\n" +
				"}\n";
		css += "body {";
		css += "font-family: " + font + ";";
		css += "font-size: " + fontSize + "px;";
		css += "font-style: " + fontStyle + ";";
		css += "color: #" + fontColor.toString().substring(2) + ";";
		css += "text-align:center;";
		css += "}\n";

		css += "table { margin-left:auto; margin-right:auto; border-spacing: 10px; }\n";
		css += "td { padding: 4px;}\n";

		String internalCSS = getOrDefault(screenSettings.getInternalSpecialCSS(), GLOBAL_SETTINGS.getInternalSpecialCSS());
		if (internalCSS != null)
			css += internalCSS;

		String externalCSS = getOrDefault(screenSettings.getExternalSpecialCSS(), GLOBAL_SETTINGS.getExternalSpecialCSS());
		if (externalCSS != null)
			css += externalCSS;

		return new ResponseEntity<>(css, new HttpHeaders(), HttpStatus.OK);
	}

	@GetMapping("/h1")
	public ResponseEntity<String> h1(@RequestParam(name = "screen") String screen, @RequestParam(name = "jury", required = false, defaultValue = "") String jury) {
		ScreenSettings screenSettings = ScreenSettings.getScreenSettingsByScreenName(screen);

		String h1 = screenSettings.getH1();
		if (h1 == null)
			h1 = GLOBAL_SETTINGS.getH1();
		Jury j = FLLController.getJuryByIdentifier(jury);
		return new ResponseEntity<>(applyReplacement(screenSettings, h1, j), new HttpHeaders(), HttpStatus.OK);
	}

	@GetMapping("/h2")
	public ResponseEntity<String> h2(@RequestParam(name = "screen") String screen, @RequestParam(name = "jury", required = false, defaultValue = "") String jury) {
		ScreenSettings screenSettings = ScreenSettings.getScreenSettingsByScreenName(screen);

		String h2 = screenSettings.getH2();
		if (h2 == null)
			h2 = GLOBAL_SETTINGS.getH2();
		Jury j = FLLController.getJuryByIdentifier(jury);
		return new ResponseEntity<>(applyReplacement(screenSettings, h2, j), new HttpHeaders(), HttpStatus.OK);
	}

	private String applyReplacement(final ScreenSettings screenSettings, final String string, final Jury jury) {
		String[] parts = string.split("\\{");
		StringBuilder result = new StringBuilder();
		for (String part : parts) {
			int i = part.indexOf("}");
			if (i > -1) {
				String key = part.substring(0, i);
				if (screenSettings instanceof JuryScreenSettings) {
					result.append(((JuryScreenSettings) screenSettings).applyReplacement(key, jury));
				} else {
					result.append(screenSettings.applyReplacement(key));
				}
				if (part.length() + 1 > i)
					result.append(part.substring(i + 1));
			} else
				result.append(part);
		}
		return result.toString();
	}

	private <T> T getOrDefault(T value, T defaultValue) {
		return value == null ? defaultValue : value;
	}

	private int getOrDefault(int value, int defaultValue) {
		return value == -1 ? defaultValue : value;
	}
}
