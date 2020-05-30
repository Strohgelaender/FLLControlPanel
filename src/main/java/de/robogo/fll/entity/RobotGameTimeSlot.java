package de.robogo.fll.entity;

import java.time.LocalTime;

public class RobotGameTimeSlot extends TimeSlot {

	private Team teamA;
	private Team teamB;
	private Table tableA;
	private Table tableB;
	private LocalTime time;
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

	public void setTableA(final Table tableA) {
		this.tableA = tableA;
	}

	public void setStatus(final Status status) {
		this.status = status;
	}

	public void setTableB(final Table tableB) {
		this.tableB = tableB;
	}

	public void setTeamA(final Team teamA) {
		this.teamA = teamA;
	}

	public void setTeamB(final Team teamB) {
		this.teamB = teamB;
	}

	public void setTime(final LocalTime time) {
		this.time = time;
	}

	public enum Status {
		Scheduled, Preparation, Running, Evaluation
	}
}
