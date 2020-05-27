package gui;

import java.io.File;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;

import org.controlsfx.control.StatusBar;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;
import struct.Importer;
import struct.RobotGameTimeSlot;
import struct.Table;
import teams.Team;

public class ControlApplication extends Application {

	private final ObjectProperty<RobotGameTimeSlot> activeSlot = new SimpleObjectProperty<>();

	private TableView<RobotGameTimeSlot> tableView;

	private static final List<Team> teams = Arrays.asList(new Team("GO Robot", 1), new Team("RoboGO", 2), new Team("NEEDSNONAME", 3));
	private List<RobotGameTimeSlot> timeSlots = Arrays.asList(new RobotGameTimeSlot(teams.get(0), teams.get(1), new Table("1"), new Table("2"), LocalTime.now()), new RobotGameTimeSlot(teams.get(1), teams.get(2), new Table("3"), new Table("4"), LocalTime.MIDNIGHT));

	private List<Team> getAllTeams() {
		//TODO das sind erstmal nur TestDaten
		return teams;
	}

	public List<RobotGameTimeSlot> getTimeSlots() {
		//TODO Testdaten
		return timeSlots;
	}

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

		// Wählen der aktuellen Runde
		Button left_arrow = new Button("<-");
		lrp.add(left_arrow, 0, 0);
		left_arrow.setOnAction(event -> {
			int i = getTimeSlots().indexOf(activeSlot.getValue());
			if (i > 0) {
				setActiveSlot(getTimeSlots().get(i - 1));
			} else {/* TODO zu vorheriger Tabelle springen*/}
		});
		Button right_arrow = new Button("->");
		lrp.add(right_arrow, 1, 0);
		right_arrow.setOnAction(event -> {
			int i = getTimeSlots().indexOf(activeSlot.getValue());
			if (i != getTimeSlots().size() - 1) {
				setActiveSlot(getTimeSlots().get(i + 1));
			} else {/* TODO zu nächster Tabelle springen*/}
		});

		//Rest

		Button download_file = new Button("Download Scoreboard");
		tbp.add(download_file, 0, 1);

		Button import_teams = new Button("Import teams and times");
		tbp.add(import_teams, 3, 0);
		import_teams.setOnAction(actionEvent -> {
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Select Timetable-Generator-File");
			fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel-Timetable", "*.xlsb"));
			File sheet = fileChooser.showOpenDialog(stage);
			Importer.importFile(sheet);
		});

		CheckBox autodelay = new CheckBox("Auto"); // einzige Checkbox
		tbp.add(autodelay, 2, 0);

		ComboBox rg_state = new ComboBox(FXCollections.observableList(Arrays.asList("Testrun", "Mittagsp.", "I", "II", "III", "QF", "SF", "F I", "F II", "Ende")));
		rgr.add(rg_state, 1, 0);


		// Labels
		Label delay_info = new Label("Delay:");
		dlp.add(delay_info, 0, 0);
		Label delay_value = new Label("00:00:00");
		dlp.add(delay_value, 1, 0);
		Label rgr_info = new Label("Round:");
		rgr.add(rgr_info, 0, 0);


		//Table

		tableView = new TableView<>();
		TableColumn<RobotGameTimeSlot, LocalTime> time = new TableColumn<>("Time");
		time.setCellValueFactory(new PropertyValueFactory<>("time"));
		time.setCellFactory(robotGameTimeSlotStringTableColumn -> {
			TableCell<RobotGameTimeSlot, LocalTime> cell = new TableCell<>() {
				@Override
				protected void updateItem(final LocalTime item, final boolean empty) {
					if (item != null) {
						setText(String.format("%s:%s", addZerosToNumber(item.getHour()), addZerosToNumber(item.getMinute())));
					}
				}
			};
			return cell;
		});

		TableColumn<RobotGameTimeSlot, Property<Team>> teamA = new TableColumn<>("Team");
		teamA.setCellValueFactory(i -> createTeamValue(i, t -> t.getTeamA()));
		teamA.setCellFactory(this::createTeamComboBoxCell);

		TableColumn<RobotGameTimeSlot, Table> tableA = new TableColumn<>("TischA");
		tableA.setCellValueFactory(new PropertyValueFactory<>("tableA"));

		TableColumn<RobotGameTimeSlot, Table> tableB = new TableColumn<>("TischB");
		tableB.setCellValueFactory(new PropertyValueFactory<>("tableB"));

		TableColumn<RobotGameTimeSlot, Property<Team>> teamB = new TableColumn<>("Team");
		teamB.setCellValueFactory(i -> createTeamValue(i, t -> t.getTeamB()));
		teamB.setCellFactory(this::createTeamComboBoxCell);

		tableView.setRowFactory(param -> {
			TableRow<RobotGameTimeSlot> row = new TableRow<>();

			BooleanBinding active = row.itemProperty().isEqualTo(activeSlot);
			row.styleProperty().bind(Bindings.when(active)
					.then(" -fx-background-color: lightgreen ;")
					.otherwise(""));

			return row;
		});

		tableView.setItems(FXCollections.observableList(getTimeSlots()));

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
			statusBar.setText(String.format("%s:%s:%s", addZerosToNumber(localTime.getHour()), addZerosToNumber(localTime.getMinute()), addZerosToNumber(localTime.getSecond())));
		}), new KeyFrame(Duration.seconds(1)));
		clock.setCycleCount(Animation.INDEFINITE);
		clock.play();

		windowRoot.getChildren().addAll(root, statusBar);
		AnchorPane.setBottomAnchor(statusBar, 0.0);
		AnchorPane.setLeftAnchor(statusBar, 0.0);
		AnchorPane.setRightAnchor(statusBar, 0.0);

		Scene scene = new Scene(windowRoot);

		stage.setScene(scene);

		setActiveSlot(getTimeSlots().get(0));

		stage.setTitle("KuC Control Ball");
		stage.setWidth(1150);
		stage.setHeight(650);
		stage.show();
	}

	private String addZerosToNumber(int num) {
		if (num < 10)
			return "0" + num;
		return "" + num;
	}

	private ObservableValue<Property<Team>> createTeamValue(TableColumn.CellDataFeatures<RobotGameTimeSlot, Property<Team>> i, Callback<RobotGameTimeSlot, Team> tcb) {
		return Bindings.createObjectBinding(() -> new SimpleObjectProperty<>(tcb.call(i.getValue())));
	}

	private TableCell<RobotGameTimeSlot, Property<Team>> createTeamComboBoxCell(TableColumn<RobotGameTimeSlot, Property<Team>> col) {
		TableCell<RobotGameTimeSlot, Property<Team>> cell = new TableCell<>();
		ComboBox<Team> comboBox = new ComboBox<>(FXCollections.observableList(getAllTeams()));
		cell.itemProperty().addListener((observable, oldValue, newValue) -> {
			if (oldValue != null) {
				comboBox.valueProperty().unbindBidirectional(oldValue);
			}
			if (newValue != null) {
				comboBox.valueProperty().bindBidirectional(newValue);
			}
		});
		cell.graphicProperty().bind(Bindings.when(cell.emptyProperty()).then((Node) null).otherwise(comboBox));
		return cell;
	}

	public static void main(String[] args) {
		launch(args);
	}

	public void setActiveSlot(final RobotGameTimeSlot activeSlot) {
		this.activeSlot.setValue(activeSlot);
	}
}
