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

import com.jfoenix.controls.JFXTimePicker;

import de.robogo.fll.control.FLLController;
import de.robogo.fll.control.ScoreboardDownloader;
import de.robogo.fll.entity.Jury;
import de.robogo.fll.entity.RoundMode;
import de.robogo.fll.entity.Table;
import de.robogo.fll.entity.Team;
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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
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
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.Pair;

@Component
public class ControlApplication extends Application {

	private static ConfigurableApplicationContext context;

	private static final DateTimeFormatter HHmmFormatter = DateTimeFormatter.ofPattern("HH:mm");
	private static final DateTimeFormatter HHmmssFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

	private TableView<TimeSlot> tableView;
	private final List<TableColumn<TimeSlot, ?>> robotGameTableColumns = new ArrayList<>();
	private final List<TableColumn<TimeSlot, ?>> juryTableColumns = new ArrayList<>();

	private ComboBox<RoundMode> rg_state;
	private StatusBar statusBar;

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

		Button left_arrow = new Button("<-");
		lrp.add(left_arrow, 0, 0);
		left_arrow.setOnAction(generateArrowEventHandler(-1));
		Button right_arrow = new Button("->");
		lrp.add(right_arrow, 1, 0);
		right_arrow.setOnAction(generateArrowEventHandler(1));

		Button export_file = new Button("Export Tournament");
		tbp.add(export_file, 3, 1);
		export_file.setOnAction(event -> {
			RoboGoExporter exporter = new RoboGoExporter();
			statusBar.progressProperty().bind(exporter.progressProperty());
			exporter.setOnSucceeded(event1 -> unbindProgressProperty());
			exporter.setOnFailed(event1 -> unbindProgressProperty());
			new Thread(exporter).start();
		});

		Button download_file = new Button("Download Scoreboard");
		tbp.add(download_file, 0, 1);

		Button import_teams = new Button("Import teams and times");
		tbp.add(import_teams, 3, 0);
		import_teams.setOnAction(actionEvent -> {
			if (!FLLController.getTeams().isEmpty()) {
				Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION, "continue?", ButtonType.YES, ButtonType.NO);
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
					dialog.show();
				});
				importer.setOnSucceeded(event -> {
					refreshTable();
					unbindProgressProperty();
				});
				new Thread(importer).start();
				statusBar.progressProperty().bind(importer.progressProperty());
			} else {
				statusBar.progressProperty().set(0);
			}
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

		statusBar = new StatusBar();
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

		RoboGoImporter autoimporter = new RoboGoImporter();
		statusBar.progressProperty().bind(autoimporter.progressProperty());
		autoimporter.setOnSucceeded(event -> {
			refreshTable();
			unbindProgressProperty();
		});
		autoimporter.setOnFailed(event -> unbindProgressProperty());
		new Thread(autoimporter).start();
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
			tableView.setItems(getTimeSlots().filtered(timeSlot -> timeSlot instanceof JurySlot));
			//TODO rename and change to own enum / remove from RoundMode (Import!)
		} else {
			tableView.getColumns().addAll(robotGameTableColumns);
			tableView.setItems(getTimeSlots().filtered(timeSlot -> timeSlot instanceof RobotGameSlot && ((RobotGameSlot) timeSlot).getRoundMode().equals(rg_state.getValue())));
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
			}
		};
		final Label pause = new Label("Pause");
		cell.tableRowProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null) {
				newValue.itemProperty().addListener((observable1, oldValue1, newValue1) -> {
					if (newValue1 instanceof PauseTimeSlot) {
						cell.setGraphic(pause);
						newValue.setEditable(false);
						cell.setEditable(false);
					} else if (newValue1 != null) {
						newValue.setEditable(true);
						cell.setEditable(true);
					} else {
						cell.setGraphic(null);
					}
				});
			}
		});
		cell.setOnMouseClicked(event -> {
			if (event.getButton().equals(MouseButton.PRIMARY) && !cell.isEditing()) {
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
			if (rg_state.getValue() == RoundMode.TestRound) {
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
				//TODO das ändert sich noch
				int i = rg_state.getValue().ordinal() + adder;
				if (i >= 0 && i < RoundMode.values().length) {
					rg_state.setValue(RoundMode.values()[i]);
					refreshTable();
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
