package de.robogo.fll.entity.timeslot;

import java.time.LocalTime;

import de.robogo.fll.entity.RoundMode;
import de.robogo.fll.entity.Table;
import de.robogo.fll.entity.Team;

public class RobotGameTimeSlot extends TimeSlot implements RobotGameSlot {

	private Team teamA;
	private Team teamB;
	private Table tableA;
	private Table tableB;
	private RoundMode roundMode;
	private Status status;

	public RobotGameTimeSlot() {
		//default constructor for object mapping
	}

	public RobotGameTimeSlot(final Team teamA, final Team teamB, final Table tableA, final Table tableB, final LocalTime time, RoundMode roundMode) {
		super(time);
		this.teamA = teamA;
		this.teamB = teamB;
		this.tableA = tableA;
		this.tableB = tableB;
		this.roundMode = roundMode;
		status = Status.Scheduled;
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

	public void setRoundMode(final RoundMode roundMode) {
		this.roundMode = roundMode;
	}

	public enum Status {
		Scheduled, Preparation, Running, Evaluation
	}
}
