package de.robogo.fll.entity;

import java.time.LocalTime;

public class JuryTimeSlot extends TimeSlot {

	private Team team;
	private final Jury jury;

	public JuryTimeSlot(final Team team, final LocalTime time, final Jury jury) {
		super(time);
		this.team = team;
		this.jury = jury;
	}

	public Team getTeam() {
		return team;
	}

	public void setTeam(final Team team) {
		this.team = team;
	}

	public Jury getJury() {
		return jury;
	}
}
