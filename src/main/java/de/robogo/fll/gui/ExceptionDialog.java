package de.robogo.fll.gui;

import java.io.PrintWriter;
import java.io.StringWriter;

import com.jfoenix.controls.JFXTextArea;

import javafx.scene.control.Alert;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

//TODO show StackTrace and causes
public class ExceptionDialog extends Alert {

	public ExceptionDialog(final Throwable e) {
		super(AlertType.ERROR);
		setTitle("Error");
		setHeaderText("An error occurred");
		setContentText(e.getMessage());

		GridPane expandableContent = new GridPane();

		JFXTextArea textArea = new JFXTextArea(extractStackTrace(e));
		textArea.setEditable(false);
		textArea.setWrapText(true);

		textArea.setMaxWidth(Double.MAX_VALUE);
		textArea.setMaxHeight(Double.MAX_VALUE);
		GridPane.setVgrow(textArea, Priority.ALWAYS);
		GridPane.setHgrow(textArea, Priority.ALWAYS);

		expandableContent.add(textArea, 0, 1);

		getDialogPane().setExpandableContent(textArea);
	}

	private String extractStackTrace(Throwable e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		return sw.toString();
	}
}
