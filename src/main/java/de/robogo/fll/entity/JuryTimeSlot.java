package de.robogo.fll.entity;

import java.time.LocalTime;

public class JuryTimeSlot extends TimeSlot {

	private Team team;
	private final JuryType juryType;
	private String location;
	private int juryNumber;

	public JuryTimeSlot(final Team team, final LocalTime time, final JuryType juryType, final String location, final int juryNumber) {
		super(time);
		this.team = team;
		this.juryType = juryType;
		this.location = location;
		this.juryNumber = juryNumber;
	}

	public int getJuryNumber() {
		return juryNumber;
	}

	public JuryType getJuryType() {
		return juryType;
	}

	public String getLocation() {
		return location;
	}

	public Team getTeam() {
		return team;
	}

	public void setTeam(final Team team) {
		this.team = team;
	}

	public void setLocation(final String location) {
		this.location = location;
	}

	public void setJuryNumber(final int juryNumber) {
		this.juryNumber = juryNumber;
	}

	public enum JuryType {
		TestRound, RobotDesign, Teamwork, Research
	}

}
