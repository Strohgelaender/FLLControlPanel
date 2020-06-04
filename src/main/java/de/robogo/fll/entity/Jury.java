package de.robogo.fll.entity;

public class Jury {

	private final JuryType juryType;
	private int num;
	private final String room;

	public Jury(final JuryType juryType, final int num, final String room) {
		this.juryType = juryType;
		this.num = num;
		this.room = room;
	}

	public JuryType getJuryType() {
		return juryType;
	}

	public int getNum() {
		return num;
	}

	public String getRoom() {
		return room;
	}

	public enum JuryType {
		TestRound("TR", "Test Round"),
		RobotDesign("R", "Robot Design"),
		Teamwork("T", "Teamwork"),
		Research("F", "Research"),
		LiveChallenge("LC", "Live Challenge");

		private final String shortName;
		private final String longName;

		JuryType(String shortText, final String longName) {
			this.shortName = shortText;
			this.longName = longName;
		}

		public String getShortName() {
			return shortName;
		}

		public String getLongName() {
			return longName;
		}
	}
}
