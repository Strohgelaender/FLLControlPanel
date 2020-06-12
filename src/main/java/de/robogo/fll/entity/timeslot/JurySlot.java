package de.robogo.fll.entity.timeslot;

import java.time.LocalTime;

/**
 * interface for common methods in {@link JuryTimeSlot} and {@link JuryPauseTimeSlot}
 */
public interface JurySlot {

	LocalTime getTime();
}
