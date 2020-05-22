package teams;

public class Team {

	private String name;
	private int id;
	private int round1;
	private int round2;
	private int round3;
	private int rank;
	private int QF;

	public Team(String name, int id) {
		this.name = name;
		this.id = id;
		this.QF = Integer.MIN_VALUE;
	}

	public int getId() {
		return id;
	}

	public int getRound1() {
		return round1;
	}

	public void setRound1(final int round1) {
		this.round1 = round1;
	}

	public int getRound2() {
		return round2;
	}

	public void setRound2(final int round2) {
		this.round2 = round2;
	}

	public int getRound3() {
		return round3;
	}

	public void setRound3(final int round3) {
		this.round3 = round3;
	}

	public String getName() {
		return name;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(final int rank) {
		this.rank = rank;
	}

	public void setRound(int round, int points) {
		switch (round) {
			case 1:
				setRound1(points);
				break;
			case 2:
				setRound2(points);
				break;
			case 3:
				setRound3(points);
				break;
			default:
				throw new IllegalArgumentException();
		}
	}

	public int getBestRoundPoints() {
		return Integer.max(Integer.max(round1, round2), round3);
	}

	public int getBestRoundNumber() {
		int bestPoints = getBestRoundPoints();
		if (bestPoints == round1)
			return 1;
		else if (bestPoints == round2)
			return 2;
		else
			return 3;
	}

	public int getQF() {
		return QF;
	}

	public void setQF(final int QF) {
		this.QF = QF;
	}

	@Override
	public String toString() {
		return name + " [" + id + "] ";
	}

	public int getInternalNumber() {
		switch (name) {
			case "Club der dichten Toten":
				return 1;
			case "JFG Girls Power":
				return 2;
			case "Bvs Robots 1":
				return 3;
			case "Parasite":
				return 4;
			case "JFG Unicorn Power":
				return 5;
			case "BvS Robots 2":
				return 6;
			case "TÜF":
				return 7;
			case "JFGTechnology":
				return 8;
			case "MPG IT Girls":
				return 9;
			case "AKRobotics":
				return 10;
			case "MINT":
				return 11;
			case "MPG Roboboys":
				return 12;
			case "Fiesta Mexicana":
				return 13;
			case "Grasser Robotics":
				return 14;
			case "TechNoLogic":
				return 15;
			case "AufBau":
				return 16;
			case "RobotIKG":
				return 17;
			case "RoboRO":
				return 18;
			case "NEEDS NO NAME":
				return 19;
			case "PaRaMeRos":
				return 20;
			case "GO ROBOT":
				return 21;
			case "GB RobotChamps":
				return 22;
			case "Die Ilmtaler":
				return 23;
		}
		return 0;
	}
}
