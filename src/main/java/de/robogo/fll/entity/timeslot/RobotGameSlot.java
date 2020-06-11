package de.robogo.fll.entity.timeslot;

import java.time.LocalTime;

import de.robogo.fll.entity.RoundMode;

//marker interface
public interface RobotGameSlot {

	LocalTime getTime();

	RoundMode getRoundMode();
}
