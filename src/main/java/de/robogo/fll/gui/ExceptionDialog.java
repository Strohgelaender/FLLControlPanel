package de.robogo.fll.gui;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public class ExceptionDialog extends Alert {

	public ExceptionDialog(final Throwable e) {
		super(AlertType.ERROR, "Ein Fehler ist aufgetreten", ButtonType.OK);
		setContentText(e.getMessage());
	}
}
