package de.robogo.fll.entity.timeslot;

import java.time.LocalTime;

public abstract class PauseTimeSlot extends TimeSlot {

	public PauseTimeSlot() {
		//default constructor for object mapping
	}

	public PauseTimeSlot(final LocalTime time) {
		super(time);
	}
}
