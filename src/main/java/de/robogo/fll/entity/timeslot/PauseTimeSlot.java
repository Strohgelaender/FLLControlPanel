package de.robogo.fll.entity.timeslot;

import java.time.LocalTime;

/**
 * Abstract class representing any pauses
 */
public abstract class PauseTimeSlot extends TimeSlot {

	public PauseTimeSlot() {
		//default constructor for object mapping
	}

	public PauseTimeSlot(final LocalTime time) {
		super(time);
	}
}
