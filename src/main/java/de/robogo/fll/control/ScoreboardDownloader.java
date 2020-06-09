package de.robogo.fll.control;

import static de.robogo.fll.control.FLLController.getTeams;

import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.w3c.dom.NodeList;
import org.w3c.dom.html.HTMLAnchorElement;
import org.w3c.dom.html.HTMLElement;

import com.sun.webkit.dom.HTMLAnchorElementImpl;

import de.robogo.fll.entity.RobotGameTimeSlot;
import de.robogo.fll.entity.RoundMode;
import de.robogo.fll.entity.Team;
import de.robogo.fll.gui.HoTLoginPromptDialog;
import de.robogo.fll.io.ExcelImporter;
import javafx.concurrent.Worker;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

public final class ScoreboardDownloader {

	private static final String ADMIN_URL_PART = "tx_hotet_hotetadmin%5Bcontroller%5D=Admin";
	private static HoTLoginPromptDialog loginDialog;
	private static boolean loggedIn = false;

	private ScoreboardDownloader() {
	}

	public static void downloadScoreboard() {
		if (!loggedIn) {

			System.setProperty("http.agent", loginDialog.getEngine().getUserAgent());

			//Popup with login Prompt
			Optional<ButtonType> result = loginDialog.showAndWait();

			System.out.println(result);

			if (result.isEmpty() || result.get().getButtonData().isCancelButton()) {
				return;
			}

			if (loginDialog.getEngine().getLocation().endsWith("id=2")) {
				System.out.println("login failed!");
				//notify User?
				return;
			}
			loggedIn = true;
			System.out.println(loginDialog.getEngine().getLocation());

			loginDialog.getEngine().getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
				System.out.println("State Change: " + newValue);
				if (newValue != Worker.State.SUCCEEDED)
					return;
				else
					System.out.println(loginDialog.getEngine().getLocation());

				if (!loginDialog.getEngine().getLocation().contains(ADMIN_URL_PART)) {
					return;
				}

				String excelURL = null;

				NodeList links = loginDialog.getEngine().getDocument().getElementsByTagName("li");
				for (int i = 0; i < links.getLength(); i++) {
					HTMLElement link = (HTMLElement) links.item(i);
					if (!link.getTextContent().equals("Export Excel"))
						continue;
					excelURL = ((HTMLAnchorElement) link.getChildNodes().item(0)).getHref();
					break;
				}

				if (excelURL == null) {
					System.err.println("no Excel URL found!");
					if (!loginDialog.isShowing()) {
						loginDialog.getInfoLabel().setText("No Scores found! Please make sure you're logged in and have selected a tournament.");
						loginDialog.show();
					}
					return;
				}

				System.out.println(excelURL);

				try {
					//download Excel sheet
					URL url = new URL(excelURL);
					HttpURLConnection connection = (HttpURLConnection) url.openConnection();

					List<HttpCookie> cookies = loginDialog.getCookieManager().getCookieStore().getCookies();

					String cookieString = cookies.stream().map(HttpCookie::toString).collect(Collectors.joining("; "));

					connection.setRequestProperty("Cookie", cookieString);
					connection.setRequestProperty("User-Agent", loginDialog.getEngine().getUserAgent());

					//import excel sheet
					XSSFWorkbook wb = new XSSFWorkbook(connection.getInputStream());

					ExcelImporter.importScores(wb);

					//update QF / SF / Final - Teams
					updateQFSFRounds(RoundMode.QF, Comparator.comparingInt(Team::getRank));
					updateQFSFRounds(RoundMode.SF, Comparator.comparingInt(Team::getQF).reversed());
					updateFinalRounds();

				} catch (Exception e) {
					loginDialog.getInfoLabel().setText("Could not download Scores. Please make sure you have selected a tournament and try again");
					//If the error persists you can try this fallback option. Please not that this option is not actively supported and may contain errors.
					//TODO provide fallback option (should we really support this)
					loginDialog.show();
					e.printStackTrace();
				}
			});

			//detect opened site
			if (!loginDialog.getEngine().getLocation().contains(ADMIN_URL_PART)) {
				//TODO If überprüfen, ist das immer so?
				//navigate to start page automatically
				loginDialog.getEngine().getDocument().getElementsByTagName("nav");

				HTMLElement nav = (HTMLElement) loginDialog.getEngine().getDocument().getElementsByTagName("nav").item(0);

				for (int i = 0; i < nav.getChildNodes().getLength(); i++) {
					HTMLElement child = (HTMLElement) nav.getChildNodes().item(i);
					if (child.getTextContent().equals("Admin")) {
						String startPageURL = ((HTMLAnchorElementImpl) child).getHref();
						loginDialog.getEngine().load(startPageURL);
						break;
					}
				}
			}
		}

		if (!loginDialog.getEngine().getLoadWorker().isRunning()) {
			//reload to trigger the load listener added above
			loginDialog.getEngine().reload();
		}

	}

	private static void updateQFSFRounds(RoundMode roundMode, Comparator<Team> comparator) {
		List<RobotGameTimeSlot> timeSlots = FLLController.getTimeSlotsByRoundMode(roundMode);
		if (!timeSlots.isEmpty()) {
			getTeams().sort(comparator);
			timeSlots.sort(Comparator.comparing(RobotGameTimeSlot::getTime));
			for (int i = 0; i < timeSlots.size(); i++) {
				RobotGameTimeSlot timeSlot = timeSlots.get(i);
				timeSlot.setTeamA(getTeams().get(i));
				timeSlot.setTeamB(getTeams().get(timeSlots.size() - i - 1));
			}
		}
	}

	private static void updateFinalRounds() {
		List<RobotGameTimeSlot> timeSlots = FLLController.getTimeSlotsByRoundMode(RoundMode.Final);
		if (timeSlots.size() == 2) {
			getTeams().sort(Comparator.comparingInt(Team::getSF).reversed());
			timeSlots.sort(Comparator.comparing(RobotGameTimeSlot::getTime));
			timeSlots.get(0).setTeamA(getTeams().get(0));
			timeSlots.get(0).setTeamB(getTeams().get(1));
			timeSlots.get(1).setTeamA(getTeams().get(1));
			timeSlots.get(1).setTeamB(getTeams().get(0));
		}
	}

	public static void initLoginDialog(Stage stage) {
		if (loginDialog != null)
			throw new IllegalStateException("Dialog already initialized");
		loginDialog = new HoTLoginPromptDialog(stage);
	}
}
