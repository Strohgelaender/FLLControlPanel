package de.robogo.fll.gui;

import java.net.CookieHandler;
import java.net.CookieManager;

import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Window;

public class HoTLoginPromptDialog extends Dialog<ButtonType> {

	private static final String url = "http://test.et.hands-on-technology.de";
	private static final CookieManager cookieManager = new CookieManager();

	private final WebEngine engine;
	private final Label info;

	static {
		CookieHandler.setDefault(cookieManager);
	}

	public HoTLoginPromptDialog(Window window) {

		initModality(Modality.APPLICATION_MODAL);
		initOwner(window);

		DialogPane dialogPane = new DialogPane();

		GridPane root = new GridPane();

		info = new Label("Please login to the HoT System");

		root.add(info, 0, 0);

		WebView browser = new WebView();
		engine = browser.getEngine();

		engine.load(url);

		root.add(browser, 0, 1);

		dialogPane.setContent(root);

		dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

		setDialogPane(dialogPane);
	}

	public WebEngine getEngine() {
		return engine;
	}

	public CookieManager getCookieManager() {
		return cookieManager;
	}

	public Label getInfoLabel() {
		return info;
	}
}
