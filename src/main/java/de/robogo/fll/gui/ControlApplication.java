package de.robogo.fll.gui;

import static de.robogo.fll.control.FLLController.getActiveSlot;
import static de.robogo.fll.control.FLLController.getActiveTime;
import static de.robogo.fll.control.FLLController.getTimeSlots;
import static de.robogo.fll.control.FLLController.setActiveTime;

import java.io.File;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.controlsfx.control.StatusBar;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTimePicker;

import de.robogo.fll.control.FLLController;
import de.robogo.fll.control.ScoreboardDownloader;
import de.robogo.fll.entity.Jury;
import de.robogo.fll.entity.RoundMode;
import de.robogo.fll.entity.Table;
import de.robogo.fll.entity.Team;
import de.robogo.fll.entity.TimeMode;
import de.robogo.fll.entity.timeslot.EventTimeSlot;
import de.robogo.fll.entity.timeslot.JurySlot;
import de.robogo.fll.entity.timeslot.JuryTimeSlot;
import de.robogo.fll.entity.timeslot.PauseTimeSlot;
import de.robogo.fll.entity.timeslot.RobotGameSlot;
import de.robogo.fll.entity.timeslot.RobotGameTimeSlot;
import de.robogo.fll.entity.timeslot.TimeSlot;
import de.robogo.fll.io.ExcelImporter;
import de.robogo.fll.io.RoboGoExporter;
import de.robogo.fll.io.RoboGoImporter;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.Pair;

@Component
public class ControlApplication extends Application {

	private static final String APPLICATION_NAME = "FLL Control Panel";

	private static ConfigurableApplicationContext context;

	private static final DateTimeFormatter HHmmFormatter = DateTimeFormatter.ofPattern("HH:mm");
	private static final DateTimeFormatter HHmmssFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

	private Stage stage;

	private TableView<TimeSlot> tableView;
	private final List<TableColumn<TimeSlot, ?>> robotGameTableColumns = new ArrayList<>();
	private final List<TableColumn<TimeSlot, ?>> juryTableColumns = new ArrayList<>();
	private final List<TableColumn<TimeSlot, ?>> eventTableColumns = new ArrayList<>();

	private ComboBox<TimeMode> timeModeCB;
	private ComboBox<RoundMode> roundModeCB;
	private StatusBar statusBar;

	private RoundMode lastRoundMode;

	@Override
	public void start(final Stage stage) {

		this.stage = stage;

		// root anchor pane
		// contains main VBox
		// and status bar at bottom
		AnchorPane windowRoot = new AnchorPane();

		//root VBox
		//HBox 1: Control Buttons
		//HBox 2: perv / next Buttons
		//Table
		//HBox 3: import / export Buttons
		VBox root = new VBox();
		root.setPadding(new Insets(5));
		root.setAlignment(Pos.CENTER);

		HBox controlButtons = generateControlButtons();
		HBox nextButtons = generateNextButtons();
		initTable();
		HBox ioButtons = generateIOButtons();

		root.getChildren().addAll(controlButtons, nextButtons, tableView, ioButtons);

		createStatusBar();

		windowRoot.getChildren().addAll(root, statusBar);

		Scene scene = new Scene(windowRoot);

		stage.setScene(scene);

		ScoreboardDownloader.initLoginDialog(stage);
		timeModeCB.getSelectionModel().selectFirst(); //call after Table-Init!

		stage.setTitle(APPLICATION_NAME);
		stage.setWidth(1020);
		stage.setHeight(650);
		stage.show();

		RoboGoImporter autoImporter = new RoboGoImporter();
		statusBar.progressProperty().bind(autoImporter.progressProperty());
		autoImporter.setOnSucceeded(event -> {
			Optional<TimeSlot> ts = FLLController.getActiveSlot();
			if (ts.isPresent()) {
				TimeSlot timeSlot = ts.get();
				timeModeCB.setValue(timeSlot.getTimeMode());
				if (timeSlot instanceof RobotGameSlot)
					roundModeCB.setValue(((RobotGameSlot) timeSlot).getRoundMode());
			}
			refreshTable();
			unbindProgressProperty();
			stage.setTitle(APPLICATION_NAME + " - " + FLLController.getEventName());
		});
		autoImporter.setOnFailed(event -> unbindProgressProperty());
		new Thread(autoImporter).start();
	}

	private HBox generateControlButtons() {
		HBox cb = new HBox();
		cb.setSpacing(10);
		cb.setPadding(new Insets(20, 5, 5, 5));
		cb.setOpaqueInsets(new Insets(5));
		cb.setAlignment(Pos.TOP_CENTER);

		Label currentEvent = new Label("current Event");

		timeModeCB = new JFXComboBox<>(FXCollections.observableArrayList(TimeMode.values()));
		timeModeCB.getSelectionModel().selectedItemProperty().addListener(this::onTimeModeChange);

		roundModeCB = new JFXComboBox<>(FXCollections.observableArrayList(RoundMode.values()));
		roundModeCB.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> refreshTable());

		final CheckBox autoSwitch = new JFXCheckBox();
		autoSwitch.setText("auto switch");
		autoSwitch.setSelected(true);

		Label delay = new Label("Delay: 00:00:00");

		cb.getChildren().addAll(currentEvent, timeModeCB, roundModeCB, autoSwitch, delay);
		return cb;
	}

	private HBox generateNextButtons() {
		HBox nb = new HBox();
		nb.setAlignment(Pos.TOP_CENTER);
		nb.setSpacing(30);
		nb.setPadding(new Insets(5));

		JFXButton left_arrow = new JFXButton("<-");
		left_arrow.setOnAction(generateArrowEventHandler(-1));
		left_arrow.setPrefWidth(100);
		left_arrow.setButtonType(JFXButton.ButtonType.RAISED);

		JFXButton right_arrow = new JFXButton("->");
		right_arrow.setOnAction(generateArrowEventHandler(1));
		right_arrow.setPrefWidth(100);
		right_arrow.setButtonType(JFXButton.ButtonType.RAISED);

		nb.getChildren().addAll(left_arrow, right_arrow);
		return nb;
	}

	private HBox generateIOButtons() {
		HBox io = new HBox();
		io.setSpacing(100);
		io.setPadding(new Insets(30, 5, 5, 5));
		io.setAlignment(Pos.CENTER);

		JFXButton import_teams = new JFXButton("Import teams and times");
		import_teams.setOnAction(actionEvent -> callExcelImporter());
		import_teams.setButtonType(JFXButton.ButtonType.RAISED);

		JFXButton download_file = new JFXButton("Download Scoreboard");
		download_file.setOnAction(event -> ScoreboardDownloader.downloadScoreboard());
		download_file.setButtonType(JFXButton.ButtonType.RAISED);

		JFXButton export_file = new JFXButton("Export Tournament");
		export_file.setOnAction(event -> callExporter());
		export_file.setButtonType(JFXButton.ButtonType.RAISED);

		JFXButton settings = new JFXButton("Settings");
		settings.setButtonType(JFXButton.ButtonType.RAISED);

		io.getChildren().addAll(import_teams, download_file, export_file, settings);
		return io;
	}

	private void createStatusBar() {
		statusBar = new StatusBar();
		statusBar.setText("13:42:44");

		Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, actionEvent -> {
			LocalTime localTime = LocalTime.now();
			statusBar.setText(HHmmssFormatter.format(localTime));
		}), new KeyFrame(Duration.seconds(1)));
		clock.setCycleCount(Animation.INDEFINITE);
		clock.play();

		AnchorPane.setBottomAnchor(statusBar, 0.0);
		AnchorPane.setLeftAnchor(statusBar, 0.0);
		AnchorPane.setRightAnchor(statusBar, 0.0);
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

	private void callExcelImporter() {
		if (!FLLController.getTeams().isEmpty()) {
			Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION, "continue?", ButtonType.YES, ButtonType.NO);
			confirmation.initOwner(stage);
			confirmation.setContentText("This will overwrite all existing data. Do you really want to continue?");
			Optional<ButtonType> type = confirmation.showAndWait();
			if (type.isEmpty() || type.get().equals(ButtonType.NO))
				return;
		}

		statusBar.progressProperty().set(-1);
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Select Timetable-Generator-File");
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel-Timetable", "*.xlsb"));
		File file = fileChooser.showOpenDialog(stage);

		if (file != null) {
			ExcelImporter importer = new ExcelImporter(file);
			importer.setOnFailed(event -> {
				unbindProgressProperty();
				ExceptionDialog dialog = new ExceptionDialog(event.getSource().getException());
				dialog.initOwner(stage);
				dialog.show();
			});
			importer.setOnSucceeded(event -> {
				refreshTable();
				unbindProgressProperty();
				stage.setTitle(APPLICATION_NAME + " - " + FLLController.getEventName());
			});
			new Thread(importer).start();
			statusBar.progressProperty().bind(importer.progressProperty());
		} else {
			statusBar.progressProperty().set(0);
		}
	}

	private void callExporter() {
		RoboGoExporter exporter = new RoboGoExporter();
		statusBar.progressProperty().bind(exporter.progressProperty());
		exporter.setOnSucceeded(event1 -> unbindProgressProperty());
		exporter.setOnFailed(event1 -> unbindProgressProperty());
		new Thread(exporter).start();
	}

	private void onTimeModeChange(ObservableValue<? extends TimeMode> observableValue, TimeMode oldTime, TimeMode newTime) {
		if (newTime != TimeMode.RobotGame) {
			lastRoundMode = roundModeCB.getValue();
			roundModeCB.setValue(null);
			roundModeCB.setDisable(true);
		} else {
			if (lastRoundMode == null)
				roundModeCB.setValue(RoundMode.Round1);
			else if (lastRoundMode.ordinal() + 1 < RoundMode.values().length)
				roundModeCB.setValue(RoundMode.values()[lastRoundMode.ordinal() + 1]);
			roundModeCB.setDisable(false);
		}
		refreshTable();
	}

	public void refreshTable() {
		tableView.edit(-1, null);
		tableView.getColumns().remove(1, tableView.getColumns().size());
		if (timeModeCB.getValue() == TimeMode.JudgingSessions) {
			tableView.getColumns().addAll(juryTableColumns);
			tableView.setItems(getTimeSlots().filtered(timeSlot -> timeSlot instanceof JurySlot));
		} else if (timeModeCB.getValue() == TimeMode.RobotGame) {
			tableView.getColumns().addAll(robotGameTableColumns);
			tableView.setItems(getTimeSlots().filtered(timeSlot -> timeSlot instanceof RobotGameSlot && ((RobotGameSlot) timeSlot).getRoundMode().equals(roundModeCB.getValue())));
		} else {
			tableView.getColumns().addAll(eventTableColumns);
			tableView.setItems(getTimeSlots().filtered(timeSlot -> timeSlot instanceof EventTimeSlot && timeSlot.getTimeMode() == timeModeCB.getValue()));
		}
		tableView.refresh();
	}

	private static final int PREF_TEAM_COLUMN_WIDTH = 200;
	private static final ObservableList<Jury.JuryType> juryTypes = FXCollections.observableList(Arrays.asList(Jury.JuryType.values()));

	private void initTable() {
		tableView = new TableView<>();

		TableColumn<TimeSlot, LocalTime> time = new TableColumn<>("Time");
		time.setCellValueFactory(new PropertyValueFactory<>("time"));
		time.setCellFactory(p -> createTimePickerCell());

		//RobotGame Columns
		TableColumn<TimeSlot, Team> teamA = new TableColumn<>("Team");
		teamA.setCellValueFactory(param -> {
			if (param.getValue() instanceof RobotGameTimeSlot)
				return new SimpleObjectProperty<>(((RobotGameTimeSlot) param.getValue()).getTeamA());
			return null;
		});
		teamA.setCellFactory(param -> createComboBoxCell(FLLController.getTeams(), p -> ((RobotGameTimeSlot) p.getKey()).setTeamA(p.getValue())));

		TableColumn<TimeSlot, Table> tableA = new TableColumn<>("TischA");
		tableA.setCellValueFactory(param -> {
			if (param.getValue() instanceof RobotGameTimeSlot)
				return new SimpleObjectProperty<>(((RobotGameTimeSlot) param.getValue()).getTableA());
			return null;
		});
		tableA.setCellFactory(param -> createComboBoxCell(FLLController.getTables(), p -> ((RobotGameTimeSlot) p.getKey()).setTableA(p.getValue())));
		//TODO only used tables (?)

		TableColumn<TimeSlot, Table> tableB = new TableColumn<>("TischB");
		tableB.setCellValueFactory(param -> {
			if (param.getValue() instanceof RobotGameTimeSlot)
				return new SimpleObjectProperty<>(((RobotGameTimeSlot) param.getValue()).getTableB());
			return null;
		});
		tableB.setCellFactory(param -> createComboBoxCell(FLLController.getTables(), p -> ((RobotGameTimeSlot) p.getKey()).setTableB(p.getValue())));

		TableColumn<TimeSlot, Team> teamB = new TableColumn<>("Team");
		teamB.setCellValueFactory(param -> {
			if (param.getValue() instanceof RobotGameTimeSlot)
				return new SimpleObjectProperty<>(((RobotGameTimeSlot) param.getValue()).getTeamB());
			return null;
		});
		teamB.setCellFactory(param -> createComboBoxCell(FLLController.getTeams(), p -> ((RobotGameTimeSlot) p.getKey()).setTeamB(p.getValue())));

		robotGameTableColumns.addAll(Arrays.asList(teamA, tableA, tableB, teamB));

		//Jury Columns

		TableColumn<TimeSlot, Team> teamJ = new TableColumn<>("Team");
		teamJ.setCellValueFactory(param -> {
			if (param.getValue() instanceof JuryTimeSlot)
				return new SimpleObjectProperty<>(((JuryTimeSlot) param.getValue()).getTeam());
			return null;
		});
		teamJ.setCellFactory(param -> createComboBoxCell(FLLController.getTeams(), p -> ((JuryTimeSlot) p.getKey()).setTeam(p.getValue())));

		TableColumn<TimeSlot, Jury.JuryType> juryType = new TableColumn<>("Jury Type");
		juryType.setCellValueFactory(param -> {
			if (param.getValue() instanceof JuryTimeSlot)
				return new SimpleObjectProperty<>(((JuryTimeSlot) param.getValue()).getJury().getJuryType());
			return null;
		});
		juryType.setCellFactory(param -> createComboBoxCell(juryTypes, p -> {
			JuryTimeSlot jts = ((JuryTimeSlot) p.getKey());
			Jury jury = FLLController.getJury(p.getValue(), jts.getJury().getNum());
			if (jury != null)
				jts.setJury(jury);
		}));

		TableColumn<TimeSlot, Integer> juryNumber = new TableColumn<>("Number");
		juryNumber.setCellValueFactory(param -> {
			if (param.getValue() instanceof JuryTimeSlot)
				return new SimpleIntegerProperty(((JuryTimeSlot) param.getValue()).getJury().getNum()).asObject();
			return null;
		});
		//TODO real jury count
		juryNumber.setCellFactory(param -> createComboBoxCell(FXCollections.observableArrayList(IntStream.range(1, FLLController.getMaxJuryNum() + 1).boxed().collect(Collectors.toList())), p -> {
			JuryTimeSlot jts = ((JuryTimeSlot) p.getKey());
			Jury jury = FLLController.getJury(jts.getJury().getJuryType(), p.getValue());
			if (jury != null)
				jts.setJury(jury);
		}));

		juryTableColumns.addAll(Arrays.asList(teamJ, juryType, juryNumber));

		TableColumn<TimeSlot, String> slotName = new TableColumn<>("Name");
		slotName.setCellValueFactory(param -> {
			if (param.getValue() instanceof EventTimeSlot)
				return new SimpleStringProperty(((EventTimeSlot) param.getValue()).getName());
			return null;
		});

		eventTableColumns.add(slotName);

		tableView.setRowFactory(param -> {
			TableRow<TimeSlot> row = new TableRow<>();

			BooleanBinding active = Bindings.createBooleanBinding(() -> FLLController.getActiveTime() != null && row.getItem() != null && FLLController.getActiveTime().equals(row.getItem().getTime()),
					row.itemProperty(), FLLController.getActiveTimeProperty());

			row.styleProperty().bind(Bindings.when(active)
					.then(" -fx-background-color: lightgreen ;")
					.otherwise(""));

			return row;
		});

		tableView.setSelectionModel(null);

		tableView.setItems(getTimeSlots());

		teamA.setPrefWidth(PREF_TEAM_COLUMN_WIDTH);
		teamB.setPrefWidth(PREF_TEAM_COLUMN_WIDTH);
		teamJ.setPrefWidth(PREF_TEAM_COLUMN_WIDTH);
		juryType.setPrefWidth(150);

		tableView.getColumns().add(time);

		tableView.setPrefWidth(1000);
		tableView.setEditable(true);
	}

	private <T> TableCell<TimeSlot, T> createComboBoxCell(final ObservableList<T> list, final Consumer<Pair<TimeSlot, T>> saveValue) {
		final TableCell<TimeSlot, T> cell = new ComboBoxTableCell<>(list) {
			@Override
			public void commitEdit(final T newValue) {
				if (getTableRow() != null && getTableRow().getItem() != null && !(getTableRow().getItem() instanceof PauseTimeSlot)) {
					saveValue.accept(new Pair<>(getTableRow().getItem(), newValue));
				}
				updateItem(newValue, false);
				cancelEdit();
			}
		};
		final Label pauseLabel = new Label("Pause");
		ChangeListener<? super TimeSlot> itemChange = (o, oldValue, newValue) -> {
			if (newValue instanceof PauseTimeSlot) {
				cell.setGraphic(pauseLabel);
				cell.getTableRow().setEditable(false);
				cell.setEditable(false);
			} else if (newValue != null) {
				cell.getTableRow().setEditable(true);
				cell.setEditable(true);
			} else {
				cell.setGraphic(null);
			}
		};
		cell.graphicProperty().addListener((observable, oldValue, newValue) -> {
			if (!(newValue instanceof Label) && cell.getTableRow() != null && cell.getTableRow().getItem() instanceof PauseTimeSlot)
				cell.setGraphic(pauseLabel);
		});
		if (cell.getTableRow() != null) {
			cell.getTableRow().itemProperty().addListener(itemChange);
			if (cell.getTableRow().getItem() != null)
				itemChange.changed(null, null, cell.getTableRow().getItem());
		}
		cell.tableRowProperty().addListener((observable, oldValue, newValue) -> {
			if (oldValue != null) {
				oldValue.itemProperty().removeListener(itemChange);
			}
			if (newValue != null) {
				if (newValue.getItem() != null)
					itemChange.changed(null, null, newValue.getItem());
				newValue.itemProperty().addListener(itemChange);
			}
		});
		cell.setOnMouseClicked(event -> {
			if (event.getButton().equals(MouseButton.PRIMARY) && !cell.isEditing() && cell.getTableRow() != null && cell.getTableRow().getItem() != null) {
				tableView.edit(cell.getTableRow().getIndex(), cell.getTableColumn());
				event.consume();
			}
		});
		return cell;
	}

	private static TableCell<TimeSlot, LocalTime> createTimePickerCell() {
		TableCell<TimeSlot, LocalTime> cell = new TableCell<>();
		JFXTimePicker timePicker = new JFXTimePicker();
		timePicker.set24HourView(true);
		cell.itemProperty().addListener((observable, oldValue, newValue) -> {
			timePicker.setValue(newValue);
		});
		timePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
			TimeSlot ts = cell.getTableRow().getItem();
			if (ts != null)
				ts.setTime(newValue);
		});
		cell.graphicProperty().bind(Bindings.when(cell.emptyProperty()).then((Node) null).otherwise(timePicker));
		return cell;
	}

	private EventHandler<ActionEvent> generateArrowEventHandler(final int adder) {
		return event -> {
			boolean nextPage = false;
			if (timeModeCB.getValue() == TimeMode.JudgingSessions) {
				//TODO das ändert sich noch (RoundMode != ZeitMode)
				NavigableMap<LocalTime, List<JurySlot>> slots = FLLController.getJuryTimeSlotsWithPauseGrouped();
				if (getActiveTime() == null) {
					if (!slots.isEmpty() && adder > 0)
						setActiveTime(slots.firstKey());
				} else {
					LocalTime current = getActiveTime();
					LocalTime next = adder > 0 ? slots.higherKey(current) : slots.lowerKey(current);
					if (next != null) {
						setActiveTime(next);
					} else {
						nextPage = true;
					}
				}
			} else {
				//TODO allow multiple RobotGames in parallel (Opens)
				List<TimeSlot> slots = tableView.getItems();
				if (getActiveTime() == null) {
					if (!slots.isEmpty() && adder > 0)
						FLLController.setActiveTime(slots.get(0));
				} else {
					Optional<TimeSlot> timeSlot = getActiveSlot();
					assert timeSlot.isPresent();
					int i = slots.indexOf(timeSlot.get());
					if (i == -1)
						FLLController.setActiveTime(slots.get(adder > 0 ? 0 : slots.size() - 1));
					else if (i + adder >= 0 && i + adder < slots.size())
						FLLController.setActiveTime(slots.get(i + adder));
					else
						nextPage = true;
				}
			}
			if (nextPage) {
				if (timeModeCB.getValue() == TimeMode.RobotGame) {
					int i = roundModeCB.getValue().ordinal() + adder;
					if (i >= 0 && i < RoundMode.values().length)
						roundModeCB.setValue(RoundMode.values()[i]);
					else
						timeModeCB.setValue(TimeMode.Closing);
				} else {
					int i = timeModeCB.getValue().ordinal() + adder;
					if (i >= 0 && i < TimeMode.values().length)
						timeModeCB.setValue(TimeMode.values()[i]);
				}
			}
		};
	}

	private void unbindProgressProperty() {
		statusBar.progressProperty().unbind();
		statusBar.progressProperty().set(0);
	}

	public static void launchApp(Class<? extends ControlApplication> appClass, ConfigurableApplicationContext context, String[] args) {
		ControlApplication.context = context;
		Application.launch(appClass, args);
	}
}
