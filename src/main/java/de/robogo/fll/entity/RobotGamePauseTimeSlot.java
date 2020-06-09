package de.robogo.fll.entity;

import java.time.LocalTime;

public class RobotGamePauseTimeSlot extends PauseTimeSlot {

	//pause on the end of this round mode
	private RoundMode roundMode;

	public RobotGamePauseTimeSlot() {
		//default constructor for object mapping
	}

	public RobotGamePauseTimeSlot(final LocalTime time, final RoundMode roundMode) {
		super(time);
		this.roundMode = roundMode;
	}

	public RoundMode getRoundMode() {
		return roundMode;
	}

	public void setRoundMode(final RoundMode roundMode) {
		this.roundMode = roundMode;
	}
}
