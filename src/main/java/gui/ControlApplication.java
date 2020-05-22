package gui;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import com.sun.javafx.scene.layout.region.LayeredBackgroundPositionConverter;
import javafx.scene.control.*;

import org.controlsfx.control.StatusBar;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import struct.RobotGameTimeSlot;
import struct.Table;
import teams.Team;

public class ControlApplication extends Application {

	@Override
	public void start(final Stage stage) throws Exception {
		// Grid panes
		AnchorPane windowRoot = new AnchorPane();

		//root Grid Pane: 0 Top Buttons, 1 Table

		GridPane root = new GridPane();

		//top buttons grid pane
		GridPane tbp = new GridPane();
		root.add(tbp, 0, 0);

		// left right buttons subpane
		GridPane lrp = new GridPane();
		tbp.add(lrp, 1, 0);

		// delay subpane
		GridPane dlp = new GridPane();
		tbp.add(dlp, 2, 1);

		// robotgameround subpane
		GridPane rgr = new GridPane();
		tbp.add(rgr, 0, 0);


		//Buttons
		Button left_arrow = new Button("links");
		lrp.add(left_arrow, 0, 0);
		Button right_arrow = new Button("rechts");
		lrp.add(right_arrow, 1, 0);
		Button download_file = new Button("Download Scoreboard");
		tbp.add(download_file, 0, 1);

		CheckBox autodelay = new CheckBox("Auto"); // einzige Checkbox
		tbp.add(autodelay, 2, 0);

		ComboBox rg_state = new ComboBox(FXCollections.observableList(Arrays.asList("1", "2", "3", "QF", "SF", "F I", "F II")));
		rgr.add(rg_state, 1, 0);

		// Labels
		Label delay_info = new Label("Delay:");
		dlp.add(delay_info, 0, 0);
		Label delay_value = new Label("00:00:00");
		dlp.add(delay_value, 1, 0);
		Label rgr_info = new Label("Round:");
		rgr.add(rgr_info, 0, 0);


		//Table

		TableView<RobotGameTimeSlot> tableView = new TableView<>();

		TableColumn<RobotGameTimeSlot, LocalTime> time = new TableColumn<>("Time");
		time.setCellValueFactory(new PropertyValueFactory<>("time"));
		time.setCellFactory(robotGameTimeSlotStringTableColumn -> new TableCell<RobotGameTimeSlot, LocalTime>() {
			@Override
			protected void updateItem(final LocalTime item, final boolean empty) {
				if (item != null) {
					setText(String.format("%s:%s", addZeorsToNumber(item.getHour()), addZeorsToNumber(item.getMinute())));
				}
			}
		});

		TableColumn<RobotGameTimeSlot, Team> teamA = new TableColumn<>("Team");
		teamA.setCellValueFactory(new PropertyValueFactory<>("teamA"));

		TableColumn<RobotGameTimeSlot, Table> tableA = new TableColumn<>("TischA");
		tableA.setCellValueFactory(new PropertyValueFactory<>("tableA"));

		TableColumn<RobotGameTimeSlot, Table> tableB = new TableColumn<>("TischB");
		tableB.setCellValueFactory(new PropertyValueFactory<>("tableB"));

		TableColumn<RobotGameTimeSlot, Team> teamB = new TableColumn<>("Team");
		teamB.setCellValueFactory(new PropertyValueFactory<>("teamB"));

		tableView.setItems(FXCollections.observableList(Collections.singletonList(new RobotGameTimeSlot(new Team("GO Robot", 1), new Team("RoboGO", 2), new Table("1"), new Table("2"), LocalTime.now()))));


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

		windowRoot.getChildren().addAll(root, statusBar);
		AnchorPane.setBottomAnchor(statusBar, 0.0);
		AnchorPane.setLeftAnchor(statusBar, 0.0);
		AnchorPane.setRightAnchor(statusBar, 0.0);

		Scene scene = new Scene(windowRoot);

		stage.setScene(scene);

		stage.setTitle("KuC Control Ball");
		stage.setWidth(1150);
		stage.setHeight(650);
		stage.show();
	}

	private String addZeorsToNumber(int num) {
		if (num < 10)
			return "0" + num;
		return "" + num;
	}

	public static void main(String[] args) {
		launch(args);
	}
}
