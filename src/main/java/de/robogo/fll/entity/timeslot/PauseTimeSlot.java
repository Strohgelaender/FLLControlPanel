package de.robogo.fll.entity.timeslot;

import java.time.LocalTime;

import de.robogo.fll.entity.TimeMode;

/**
 * Abstract class representing any pauses
 */
public abstract class PauseTimeSlot extends TimeSlot {

	public PauseTimeSlot() {
		//default constructor for object mapping
	}

	public PauseTimeSlot(final LocalTime time, TimeMode mode) {
		super(time, mode);
	}
}
