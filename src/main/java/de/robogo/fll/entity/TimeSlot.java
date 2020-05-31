package de.robogo.fll.entity;

import java.time.LocalTime;

public abstract class TimeSlot {

	private LocalTime time;

	protected TimeSlot(LocalTime time) {
		this.time = time;
	}

	public void setTime(final LocalTime time) {
		this.time = time;
	}

	public LocalTime getTime() {
		return time;
	}
}
