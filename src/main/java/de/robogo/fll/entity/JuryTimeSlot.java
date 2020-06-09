package de.robogo.fll.entity;

import java.time.LocalTime;

public class JuryTimeSlot extends TimeSlot {

	private Team team;
	private Jury jury;

	public JuryTimeSlot() {
		//default constructor for object mapping
	}

	public JuryTimeSlot(final Team team, final LocalTime time, final Jury jury) {
		super(time);
		if (jury == null)
			throw new NullPointerException();
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

	public void setJury(final Jury jury) {
		this.jury = jury;
	}
}
