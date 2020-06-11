package de.robogo.fll.entity.timeslot;

import java.time.LocalTime;

public class JuryPauseTimeSlot extends PauseTimeSlot implements JurySlot {

	public JuryPauseTimeSlot() {
		//default constructor for object mapping
	}

	public JuryPauseTimeSlot(final LocalTime time) {
		super(time);
	}
}
