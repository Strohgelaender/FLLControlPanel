package gui;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTable;
import org.apache.poi.xslf.usermodel.XSLFTableCell;
import org.apache.poi.xslf.usermodel.XSLFTableRow;
import org.apache.poi.xslf.usermodel.XSLFTextRun;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import teams.RoundMode;
import teams.Team;
import teams.TeamSelectionComboBox;
import telnet.FoobarTelnetClient;

public class MainApplication extends Application {

	private final File teamA = new File("C:\\Users\\Lucas Welscher\\Desktop\\teama.txt");
	private final File teamB = new File("C:\\Users\\Lucas Welscher\\Desktop\\teamb.txt");
	private final TeamSelectionComboBox selectTeamA = new TeamSelectionComboBox(teamA, 0);
	private final TeamSelectionComboBox selectTeamB = new TeamSelectionComboBox(teamB, 1);
	private final FoobarTelnetClient foobarTelnetClient = new FoobarTelnetClient();
	private HashMap<Integer, Team> teams = new HashMap<>();
	private File presentation = new File("C:\\Users\\Lucas Welscher\\Documents\\FLL Regio München\\Scores.pptx");

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(final Stage stage) throws Exception {
		String url = "http://et.hands-on-technology.de";

		Button selectPPP = new Button("Präsentation auswählen");
		Button output = new Button("output");

		WebView browser = new WebView();
		WebEngine engine = browser.getEngine();

		output.setOnAction(actionEvent -> {
			engine.reload();
			final String html = engine.executeScript("document.documentElement.outerHTML").toString();
			//System.out.println(html);

			new Thread(() -> {
				{ //Input Parser
					String[] table = html.substring(html.indexOf("<tbody>") + 11).split("<th scope=\"row\">");
					for (int i = 1; i <= table.length - 1; i++) { //0 ist leer???, ende: rest von der Website
						String row = table[i];
						String name = row.substring(0, row.indexOf("<br>") - 1);
						name = StringEscapeUtils.unescapeHtml3(StringEscapeUtils.unescapeHtml4(name));
						int teamID = Integer.parseInt(row.substring(row.indexOf("span class=\"team-id\">[") + 22, row.indexOf("]</span>")));

						Team team;
						if (teams.containsKey(teamID))
							team = teams.get(teamID);
						else {
							team = new Team(name, teamID);
							teams.put(teamID, team);
						}


						String[] rounds = row.split("<td>");

						for (int round = 1; round <= 3; round++) {
							String r = rounds[round].trim();
							int points;
							if (r.startsWith("<span class=\"highest\">")) {
								points = Integer.parseInt(r.substring(r.indexOf("<span class=\"highest\">") + 22, r.indexOf("</span>")));
							} else {
								points = Integer.parseInt(r.substring(0, r.indexOf("</td>")).trim());
							}
							team.setRound(round, points);
						}

						int round = 5; //4: best PR

						if (Arrays.stream(html.substring(html.indexOf("<thead>")).split("<span")).anyMatch(s -> s.endsWith("QF"))) {
							//5: QF
							team.setQF(Integer.parseInt(rounds[5].substring(0, rounds[5].indexOf("</td>")).trim()));
						}

						String rank = rounds[rounds.length - 1];
						rank = rank.substring(0, rank.indexOf("</td>")).trim();
						team.setRank(Integer.parseInt(rank));
					}
				}

				if (selectTeamA.getItems().isEmpty())
					selectTeamA.getItems().addAll(teams.values());

				if (selectTeamB.getItems().isEmpty())
					selectTeamB.getItems().addAll(teams.values());

				//TODO TODO Finalrunden auch auswerten

				List<Team> teamsOrdered = new ArrayList<>(teams.values());
				teamsOrdered.sort(Comparator.comparingInt(Team::getRank));

				{ //Output
					try {
						int t = 0;
						FileInputStream fis = new FileInputStream(presentation);
						XMLSlideShow slideShow = new XMLSlideShow(fis);
						fis.close();

						final List<XSLFSlide> slides = slideShow.getSlides();
						for (int i = 0; i < slides.size() - 1; i++) {
							final XSLFSlide slide = slides.get(i);
							Optional<XSLFShape> tab = slide.getShapes().stream().filter(xslfShape -> xslfShape instanceof XSLFTable).findFirst();
							if (tab.isPresent()) {
								XSLFTable table = (XSLFTable) tab.get();
								List<XSLFTableRow> rows = table.getRows();
								for (int r = 1; r < rows.size() && t < teamsOrdered.size(); r++) { //row 0: teams.Team / Run I / II / III / Rank
									XSLFTableRow row = rows.get(r);
									Team team = teamsOrdered.get(t++);

									setCellContent(row.getCells().get(0), team.getName());
									setCellContent(row.getCells().get(1), team.getRound1());
									setCellContent(row.getCells().get(2), team.getRound2());
									setCellContent(row.getCells().get(3), team.getRound3());
									setCellContent(row.getCells().get(4), team.getRank());

								}
								if (slides.get(slides.size() - 2) == slide) { //TODO able to have other Slides!!!
									if (teamsOrdered.size() % rows.size() != 0) {
										//TODO add new slide if to much teams
										int toMuchRows = rows.size() - (teamsOrdered.size() % rows.size());
										for (int rowToRemove = rows.size() - 1; toMuchRows > 0; rowToRemove--, toMuchRows--) {
											table.removeRow(rowToRemove);
										}
									}
								}
							}
						}

						List<Team> QFteams = teams.values().stream().filter(team -> team.getQF() > -10).sorted(Comparator.comparingInt(Team::getQF).reversed()).collect(Collectors.toList());
						//Quarter Final
						XSLFSlide QFSlide = slides.get(slides.size() - 1);
						Optional<XSLFShape> tab = QFSlide.getShapes().stream().filter(xslfShape -> xslfShape instanceof XSLFTable).findFirst();
						if (tab.isPresent()) {
							XSLFTable table = (XSLFTable) tab.get();
							List<XSLFTableRow> rows = table.getRows();
							for (int r = 1; r < rows.size() && r < QFteams.size() + 1; r++) {
								Team team = QFteams.get(r - 1);
								XSLFTableRow row = rows.get(r);

								setCellContent(row.getCells().get(0), team.getName());
								setCellContent(row.getCells().get(1), team.getQF());
								setCellContent(row.getCells().get(2), r); //TODO Rank bei Gleichstand
							}
						}


						FileOutputStream fos = new FileOutputStream(presentation);
						slideShow.write(fos);
						fos.close();

						System.out.println("Ausgabe fertig!");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}).start();
		});

		selectPPP.setOnAction(actionEvent -> {
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Präsentation auswählen");
			fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PowerPoint Präsentation", "*.pptx"));
			presentation = fileChooser.showOpenDialog(stage);
			selectPPP.setText(presentation + "");
		});

		Button selectText = new Button("Select Live Text File");
		selectText.setOnAction(actionEvent -> {
			//Write File Test
			FileChooser chooser = new FileChooser();
			File liveText = chooser.showOpenDialog(null);
			System.out.println(liveText);
			System.out.println(liveText.getAbsolutePath());
			System.out.println(liveText.exists());
			System.out.println(liveText.canWrite());
		});

		Button startTimer = new Button("\u25B6");
		startTimer.setOnAction(actionEvent -> {
		});

		Button reset = new Button("\u2b8c");
		reset.setOnAction(actionEvent -> {
		});

		HBox countdown = new HBox();
		countdown.setPadding(new Insets(5));
		countdown.setSpacing(5);
		countdown.getChildren().addAll(selectText, startTimer, reset);

		ComboBox<RoundMode> rounds = new ComboBox<>();
		rounds.getItems().addAll(RoundMode.Runde1, RoundMode.Runde2, RoundMode.Runde3, RoundMode.Viertelfinale, RoundMode.Halbfinale, RoundMode.Finale);
		rounds.getSelectionModel().selectedItemProperty().addListener((observableValue, s, t1) -> {
			selectTeamA.setRoundMode(t1);
			selectTeamB.setRoundMode(t1);
		});

		HBox teamSelect = new HBox();
		teamSelect.setPadding(new Insets(5));
		teamSelect.setSpacing(5);
		teamSelect.getChildren().addAll(selectTeamA, selectTeamB);

		Button connect = new Button("\u266B\u25B6");
		connect.setOnAction(actionEvent -> foobarTelnetClient.connect());

		Button pause = new Button("\u266B\u23F8");
		pause.setOnAction(actionEvent -> {
			if (foobarTelnetClient.isConnected()) {
				boolean active = foobarTelnetClient.isOutputActive();
				foobarTelnetClient.setActive(!active);
				if (active) {
					pause.setText("\u266A\u25B6");
				} else {
					pause.setText("\u266A\u23F8");
				}
			} else {
				System.out.println("connect Foobar Client first");
			}
		});

		Button disconnect = new Button("\u266B\u25A0");
		disconnect.setOnAction(actionEvent -> foobarTelnetClient.disconnect(true));

		HBox foobar = new HBox();
		foobar.setPadding(new Insets(5));
		foobar.setSpacing(5);
		foobar.getChildren().addAll(connect, pause, disconnect);

		VBox root = new VBox();
		root.setPadding(new Insets(5));
		root.setSpacing(5);
		root.getChildren().addAll(output, selectPPP, countdown, rounds, teamSelect, foobar, browser);

		Scene scene = new Scene(root);

		stage.setTitle("WebView Test");
		stage.setScene(scene);
		stage.show();

		Platform.runLater(() -> {
			teams = new HashMap<>();
			engine.load(url);
		});
	}

	private void setCellContent(XSLFTableCell cell, int content) {
		setCellContent(cell, content + "");
	}

	private void setCellContent(XSLFTableCell cell, String content) {
		XSLFTextRun textRun = cell.setText(content);
		textRun.setFontFamily("Open Sans Semibold");
		textRun.setFontColor(Color.WHITE);
		textRun.setFontSize(26d);
	}
}
