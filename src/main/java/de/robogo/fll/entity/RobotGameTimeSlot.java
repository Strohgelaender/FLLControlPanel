package de.robogo.fll.entity;

import java.time.LocalTime;

public class RobotGameTimeSlot extends TimeSlot {

	private final Team teamA;
	private final Team teamB;
	private final Table tableA;
	private final Table tableB;
	private final LocalTime time;
	private final RoundMode roundMode;
	private Status status;

	public RobotGameTimeSlot(final Team teamA, final Team teamB, final Table tableA, final Table tableB, final LocalTime time, RoundMode roundMode) {
		this.teamA = teamA;
		this.teamB = teamB;
		this.tableA = tableA;
		this.tableB = tableB;
		this.time = time;
		this.roundMode = roundMode;
		status = Status.Scheduled;
	}

	public LocalTime getTime() {
		return time;
	}

	public Table getTableA() {
		return tableA;
	}

	public Table getTableB() {
		return tableB;
	}

	public Team getTeamA() {
		return teamA;
	}

	public Team getTeamB() {
		return teamB;
	}

	public Status getStatus() {
		return status;
	}

	public RoundMode getRoundMode() {
		return roundMode;
	}

	public enum Status {
		Scheduled, Preparation, Running, Evaluation
	}
}
