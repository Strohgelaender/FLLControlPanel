package de.robogo.fll.entity;

import java.time.LocalTime;

public class JuryPauseTimeSlot extends PauseTimeSlot {

	public JuryPauseTimeSlot() {
		//default constructor for object mapping
	}

	public JuryPauseTimeSlot(final LocalTime time) {
		super(time);
	}
}
