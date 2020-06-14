package de.robogo.fll.entity.timeslot;

import java.time.LocalTime;

import de.robogo.fll.entity.RoundMode;
import de.robogo.fll.entity.TimeMode;

public class RobotGamePauseTimeSlot extends PauseTimeSlot implements RobotGameSlot {

	//pause on the end of this round mode
	private RoundMode roundMode;

	public RobotGamePauseTimeSlot() {
		//default constructor for object mapping
	}

	public RobotGamePauseTimeSlot(final LocalTime time, final RoundMode roundMode) {
		super(time, TimeMode.RobotGame);
		this.roundMode = roundMode;
	}

	public RoundMode getRoundMode() {
		return roundMode;
	}

	public void setRoundMode(final RoundMode roundMode) {
		this.roundMode = roundMode;
	}
}
