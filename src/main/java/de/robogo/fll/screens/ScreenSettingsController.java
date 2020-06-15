package de.robogo.fll.screens;

import static de.robogo.fll.control.ScreenSettings.GLOBAL_SETTINGS;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import de.robogo.fll.control.ScreenSettings;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

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

		Font font = screenSettings.getFont();
		if (font == null)
			font = GLOBAL_SETTINGS.getFont();
		Color fontColor = screenSettings.getFontColor();
		if (fontColor == null)
			fontColor = GLOBAL_SETTINGS.getFontColor();



		String css = "body {";
		css += "background-image: url(\"/screens/image?screen=" + screen + "\");";
		css += "font-family: " + font.getFamily() + ";";
		css += "font-size: " + font.getSize() + ";";
		css += "font-style: " + font.getStyle() + ";";
		css += "color: #" + fontColor.toString().substring(2) + ";";
		css += "}";

		if (screenSettings.getInternalSpecialCSS() != null)
			css += screenSettings.getInternalSpecialCSS();

		return new ResponseEntity<>(css, new HttpHeaders(), HttpStatus.OK);
	}
}
