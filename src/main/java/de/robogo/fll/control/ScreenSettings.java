package de.robogo.fll.control;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.compress.utils.IOUtils;
import org.springframework.http.MediaType;

import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class ScreenSettings {

	public static final ScreenSettings GLOBAL_SETTINGS = new ScreenSettings();
	public static final ScreenSettings TIMETABLE_SETTINGS = new ScreenSettings();

	private byte[] backgroundImage;
	private MediaType mediaType;

	private Font font;
	private Color fontColor;

	private String internalSpecialCSS;
	private String externalSpecialCSS;

	static {
		try {
			InputStream stream = ScreenSettings.class.getClassLoader().getResourceAsStream("PPP_Background.png");
			GLOBAL_SETTINGS.setBackgroundImage(IOUtils.toByteArray(stream));
			GLOBAL_SETTINGS.setMediaType(MediaType.IMAGE_PNG);
			GLOBAL_SETTINGS.setFont(Font.font("Arial", 30));
			GLOBAL_SETTINGS.setFontColor(Color.WHITE);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static ScreenSettings getScreenSettingsByScreenName(String screenName) {
		if (screenName.equals("timetable"))
			return TIMETABLE_SETTINGS;
		return GLOBAL_SETTINGS;
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

	public Font getFont() {
		return font;
	}

	public void setFont(final Font font) {
		this.font = font;
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
}
