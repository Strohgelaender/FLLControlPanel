package gui;

import java.time.LocalTime;

import org.controlsfx.control.StatusBar;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import struct.TimeSlot;

public class ControlApplication extends Application {

	@Override
	public void start(final Stage stage) throws Exception {

		AnchorPane windomRoot = new AnchorPane();

		//rot Grid Pane: 0 Top Buttons, 1 Table

		GridPane root = new GridPane();

		//Buttons


		//Table

		TableView<TimeSlot> tableView = new TableView<>();

		TableColumn time = new TableColumn("Time");
		TableColumn teamA = new TableColumn("Team");
		TableColumn tableA = new TableColumn("TischA");
		TableColumn tableB = new TableColumn("TischB");
		TableColumn teamB = new TableColumn("Team");

		teamA.setPrefWidth(200);

		tableView.getColumns().addAll(time, teamA, tableA, tableB, teamB);

		tableView.setPrefWidth(1150);

		tableView.setEditable(true);

		root.add(tableView, 0, 2);

		root.setPadding(new Insets(10));

		StatusBar statusBar = new StatusBar();
		statusBar.setText("13:42:44");

		Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, actionEvent -> {
			LocalTime localTime = LocalTime.now();
			statusBar.setText(String.format("%s:%s:%s", addZeorsToNumber(localTime.getHour()), addZeorsToNumber(localTime.getMinute()), addZeorsToNumber(localTime.getSecond())));
		}), new KeyFrame(Duration.seconds(1)));
		clock.setCycleCount(Animation.INDEFINITE);
		clock.play();

		windomRoot.getChildren().addAll(root, statusBar);
		AnchorPane.setBottomAnchor(statusBar, 0.0);
		AnchorPane.setLeftAnchor(statusBar, 0.0);
		AnchorPane.setRightAnchor(statusBar, 0.0);

		Scene scene = new Scene(windomRoot);

		stage.setScene(scene);

		stage.setTitle("FLL Control Panel");
		stage.setWidth(1150);
		stage.setHeight(650);
		stage.show();
	}

	private String addZeorsToNumber(int num) {
		if (num < 10)
			return "0" + num;
		return "" + num;
	}
}
