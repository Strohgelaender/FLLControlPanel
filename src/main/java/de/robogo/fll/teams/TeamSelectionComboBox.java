package de.robogo.fll.teams;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javafx.scene.control.ComboBox;

public class TeamSelectionComboBox extends ComboBox<Team> {

	private static final Path interviews = Paths.get("C:\\Users\\Lucas Welscher\\Desktop\\Interviews");
	private static final List<Integer> round2Order = Arrays.asList(2, 3, 1, 5, 4, 6, 8, 9, 7, 11, 10, 12, 14, 15, 13, 17, 16, 18, 20, 21, 19, 23, 22);
	private static final List<Integer> round3Order = Arrays.asList(3, 1, 6, 2, 4, 5, 9, 7, 12, 8, 10, 11, 15, 13, 18, 14, 16, 18, 21, 19, 20, 22, 23);
	private final Path destination;
	private RoundMode roundMode;

	public TeamSelectionComboBox(final File output, final int num) {

		destination = interviews.resolve(num + ".mp4");

		getSelectionModel().selectedItemProperty().addListener((observableValue, team, t1) -> {
			new Thread(() -> {
				try {
					FileWriter writer = new FileWriter(output);
					writer.write(t1.toString());
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

				if (roundMode != null) {
					//get Video File
					Path teamPath = interviews.resolve(t1.getName());
					System.out.println(teamPath);
					System.out.println(teamPath.toFile().exists());
					System.out.println(teamPath.toFile().isDirectory());
					System.out.println();
					teamPath = teamPath.resolve(roundMode.ordinal() + ".mp4");
					System.out.println(teamPath);
					File video = teamPath.toFile();
					System.out.println(video.exists());
					System.out.println(video.canRead());

					if (video.exists()) {
						try {
							Files.copy(teamPath, destination, StandardCopyOption.REPLACE_EXISTING);
							System.out.println("copy finished");
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
				//copy to live dir
			}).start();

		});
	}

	public void setRoundMode(final RoundMode roundMode) {
		this.roundMode = roundMode;
		switch (roundMode) {
			case Runde2:
				getItems().sort(Comparator.comparingInt(team -> round2Order.indexOf(team.getInternalNumber())));
				break;
			case Runde3:
				getItems().sort(Comparator.comparingInt(team -> round3Order.indexOf(team.getInternalNumber())));
				break;
			default:
				getItems().sort(Comparator.comparingInt(Team::getInternalNumber));
				break;
		}
	}
}
