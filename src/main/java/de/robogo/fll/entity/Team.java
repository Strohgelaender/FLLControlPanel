package de.robogo.fll.entity;

public class Team {

	private final String name;
	private final int id;
	private int round1;
	private int round2;
	private int round3;
	private int rank;
	private int QF;
	private int SF;

	public Team(String name, int id) {
		this.name = name;
		this.id = id;
		this.QF = Integer.MIN_VALUE;
		this.SF = Integer.MIN_VALUE;
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

	public void setSF(final int SF) {
		this.SF = SF;
	}

	public int getSF() {
		return SF;
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
}
