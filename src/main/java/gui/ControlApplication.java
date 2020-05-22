package gui;

import java.time.LocalTime;
import java.util.Collections;

import javafx.scene.control.CheckBox;

import org.controlsfx.control.StatusBar;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
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

		AnchorPane windomRoot = new AnchorPane();

		//rot Grid Pane: 0 Top Buttons, 1 Table

		GridPane root = new GridPane();

		//Buttons
		Button left_arrow = new Button("links");
		Button right_arrow = new Button("rechts");
		Button Download_file = new Button("Download Scoreboard");

		CheckBox Autodelay = new CheckBox("Auto"); // einzige Checkbox

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
