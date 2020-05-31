package de.robogo.fll.gui;

import static de.robogo.fll.control.FLLController.getActiveSlot;
import static de.robogo.fll.control.FLLController.getTeams;
import static de.robogo.fll.control.FLLController.getTimeSlots;
import static de.robogo.fll.control.FLLController.setActiveSlot;

import java.io.File;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.controlsfx.control.StatusBar;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import de.robogo.fll.control.FLLController;
import de.robogo.fll.control.Importer;
import de.robogo.fll.control.ScoreboardDownloader;
import de.robogo.fll.entity.JuryTimeSlot;
import de.robogo.fll.entity.RobotGameTimeSlot;
import de.robogo.fll.entity.RoundMode;
import de.robogo.fll.entity.Table;
import de.robogo.fll.entity.Team;
import de.robogo.fll.entity.TimeSlot;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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
import javafx.util.Pair;

@Component
public class ControlApplication extends Application {

	private static ConfigurableApplicationContext context;

	private static final DateTimeFormatter HHmmFormatter = DateTimeFormatter.ofPattern("HH:mm");
	private static final DateTimeFormatter HHmmssFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

	private TableView<TimeSlot> tableView;
	private ObservableList<TimeSlot> tableItems;
	private final List<TableColumn<TimeSlot, ?>> robotGameTableColumns = new ArrayList<>();
	private final List<TableColumn<TimeSlot, ?>> juryTableColumns = new ArrayList<>();

	private ComboBox<RoundMode> rg_state;

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
		left_arrow.setOnAction(generateArrowEventHandler(-1, p -> p.getKey() > 0));
		Button right_arrow = new Button("->");
		lrp.add(right_arrow, 1, 0);
		right_arrow.setOnAction(generateArrowEventHandler(1, p -> p.getKey() < p.getValue() - 1));

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
			refreshTable();
		});

		download_file.setOnAction(event -> ScoreboardDownloader.downloadScoreboard());

		CheckBox autodelay = new CheckBox("Auto"); // einzige Checkbox
		tbp.add(autodelay, 2, 0);

		rg_state = new ComboBox<>(FXCollections.observableArrayList(RoundMode.values()));
		rg_state.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> refreshTable());
		rgr.add(rg_state, 1, 0);


		// Labels
		Label delay_info = new Label("Delay:");
		dlp.add(delay_info, 0, 0);
		Label delay_value = new Label("00:00:00");
		dlp.add(delay_value, 1, 0);
		Label rgr_info = new Label("Round:");
		rgr.add(rgr_info, 0, 0);


		//Table
		initTable();

		//updates Table Columns
		rg_state.getSelectionModel().selectFirst(); //call after Table-Init!

		root.add(tableView, 0, 2);

		root.setPadding(new Insets(10));

		StatusBar statusBar = new StatusBar();
		statusBar.setText("13:42:44");


		Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, actionEvent -> {
			LocalTime localTime = LocalTime.now();
			statusBar.setText(HHmmssFormatter.format(localTime));
		}), new KeyFrame(Duration.seconds(1)));
		clock.setCycleCount(Animation.INDEFINITE);
		clock.play();

		windowRoot.getChildren().addAll(root, statusBar);
		AnchorPane.setBottomAnchor(statusBar, 0.0);
		AnchorPane.setLeftAnchor(statusBar, 0.0);
		AnchorPane.setRightAnchor(statusBar, 0.0);

		Scene scene = new Scene(windowRoot);

		stage.setScene(scene);

		ScoreboardDownloader.initLoginDialog(stage);

		stage.setTitle("KuC Control Ball");
		stage.setWidth(1150);
		stage.setHeight(650);
		stage.show();
	}

	@Override
	public void init() throws Exception {
		super.init();
		context.getAutowireCapableBeanFactory().autowireBean(this);
	}

	@Override
	public void stop() throws Exception {
		super.stop();
		context.close();
	}

	public void refreshTable() {
		tableView.getColumns().remove(1, tableView.getColumns().size());
		if (rg_state.getValue() == RoundMode.TestRound) {
			tableView.getColumns().addAll(juryTableColumns);
			tableView.setItems(tableItems.filtered(timeSlot -> timeSlot instanceof JuryTimeSlot));
			//TODO rename and change to own enum / remove from RoundMode (Import!)
		} else {
			tableView.getColumns().addAll(robotGameTableColumns);
			tableView.setItems(tableItems.filtered(timeSlot -> timeSlot instanceof RobotGameTimeSlot && ((RobotGameTimeSlot) timeSlot).getRoundMode().equals(rg_state.getValue())));
		}
		tableView.refresh();
	}

	private static final int PREF_TEAM_COLUMN_WIDTH = 200;

	private void initTable() {
		tableView = new TableView<>();

		TableColumn<TimeSlot, LocalTime> time = new TableColumn<>("Time");
		time.setCellValueFactory(new PropertyValueFactory<>("time"));
		time.setCellFactory(robotGameTimeSlotStringTableColumn -> new TableCell<>() {
			@Override
			protected void updateItem(final LocalTime item, final boolean empty) {
				if (item != null) {
					setText(HHmmFormatter.format(item));
				}
			}
		});

		//RobotGame Columns
		TableColumn<TimeSlot, Property<Team>> teamA = new TableColumn<>("Team");
		teamA.setCellValueFactory(i -> createTeamValue(i, t -> ((RobotGameTimeSlot) t).getTeamA()));
		teamA.setCellFactory(this::createTeamComboBoxCell);

		TableColumn<TimeSlot, Table> tableA = new TableColumn<>("TischA");
		tableA.setCellValueFactory(param -> {
			if (param.getValue() instanceof RobotGameTimeSlot)
				return new SimpleObjectProperty<>(((RobotGameTimeSlot) param.getValue()).getTableA());
			return null;
		});

		TableColumn<TimeSlot, Table> tableB = new TableColumn<>("TischB");
		tableB.setCellValueFactory(param -> {
			if (param.getValue() instanceof RobotGameTimeSlot)
				return new SimpleObjectProperty<>(((RobotGameTimeSlot) param.getValue()).getTableB());
			return null;
		});

		TableColumn<TimeSlot, Property<Team>> teamB = new TableColumn<>("Team");
		teamB.setCellValueFactory(i -> createTeamValue(i, t -> ((RobotGameTimeSlot) t).getTeamB()));
		teamB.setCellFactory(this::createTeamComboBoxCell);

		robotGameTableColumns.addAll(Arrays.asList(teamA, tableA, tableB, teamB));

		//Jury Columns

		TableColumn<TimeSlot, Property<Team>> teamJ = new TableColumn<>("Team");
		teamJ.setCellValueFactory(i -> createTeamValue(i, t -> ((JuryTimeSlot) t).getTeam()));
		teamJ.setCellFactory(this::createTeamComboBoxCell);

		TableColumn<TimeSlot, JuryTimeSlot.JuryType> juryType = new TableColumn<>("Jury Type");
		juryType.setCellValueFactory(param -> {
			if (param.getValue() instanceof JuryTimeSlot)
				return new SimpleObjectProperty<>(((JuryTimeSlot) param.getValue()).getJuryType());
			return null;
		});

		TableColumn<TimeSlot, Integer> juryNumber = new TableColumn<>("Number");
		juryNumber.setCellValueFactory(param -> {
			if (param.getValue() instanceof JuryTimeSlot)
				return new SimpleIntegerProperty(((JuryTimeSlot) param.getValue()).getJuryNumber()).asObject();
			return null;
		});

		juryTableColumns.addAll(Arrays.asList(teamJ, juryType, juryNumber));

		tableView.setRowFactory(param -> {
			TableRow<TimeSlot> row = new TableRow<>();

			BooleanBinding active = row.itemProperty().isEqualTo(FLLController.getActiveSlotProperty());
			row.styleProperty().bind(Bindings.when(active)
					.then(" -fx-background-color: lightgreen ;")
					.otherwise(""));

			return row;
		});

		tableItems = FXCollections.observableList(getTimeSlots());
		tableView.setItems(tableItems);

		teamA.setPrefWidth(PREF_TEAM_COLUMN_WIDTH);
		teamB.setPrefWidth(PREF_TEAM_COLUMN_WIDTH);
		teamJ.setPrefWidth(PREF_TEAM_COLUMN_WIDTH);

		tableView.getColumns().add(time);

		tableView.setPrefWidth(1000);

		tableView.setEditable(true);
	}

	private ObservableValue<Property<Team>> createTeamValue(TableColumn.CellDataFeatures<? extends TimeSlot, Property<Team>> i, Callback<TimeSlot, Team> tcb) {
		return Bindings.createObjectBinding(() -> new SimpleObjectProperty<>(tcb.call(i.getValue())));
	}

	private TableCell<TimeSlot, Property<Team>> createTeamComboBoxCell(TableColumn<TimeSlot, Property<Team>> col) {
		TableCell<TimeSlot, Property<Team>> cell = new TableCell<>();
		ComboBox<Team> comboBox = new ComboBox<>(FXCollections.observableList(getTeams()));
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

	private EventHandler<ActionEvent> generateArrowEventHandler(final int adder, final Callback<Pair<Integer, Integer>, Boolean> condition) {
		return event -> {
			List<RobotGameTimeSlot> slots = FLLController.getTimeSlotsByRoundMode(rg_state.getValue());
			if (getActiveSlot() == null) {
				if (!slots.isEmpty())
					setActiveSlot(slots.get(0));
			} else {
				int i = slots.indexOf(getActiveSlot());
				if (condition.call(new Pair<>(i, slots.size()))) {
					setActiveSlot(slots.get(i + adder));
				} else {
					//TODO zu nächster Tabelle springen
				}
			}
		};
	}

	public static void launchApp(Class<? extends ControlApplication> appClass, ConfigurableApplicationContext context, String[] args) {
		ControlApplication.context = context;
		Application.launch(appClass, args);
	}
}
