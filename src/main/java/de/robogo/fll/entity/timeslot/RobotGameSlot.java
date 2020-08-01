package de.robogo.fll.entity.timeslot;

import java.time.LocalTime;

import de.robogo.fll.entity.RoundMode;

/**
 * interface for common methods in {@link RobotGameTimeSlot} and {@link RobotGamePauseTimeSlot}
 */
public interface RobotGameSlot {

	LocalTime getTime();

	void setTime(LocalTime time);

	RoundMode getRoundMode();

	void setRoundMode(RoundMode roundMode);
}
