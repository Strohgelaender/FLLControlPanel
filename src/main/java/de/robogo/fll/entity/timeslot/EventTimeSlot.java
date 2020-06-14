package de.robogo.fll.entity.timeslot;

import java.time.LocalTime;

import de.robogo.fll.entity.TimeMode;

/**
 * TimeSlot representing any special Event
 * as shown on the timetable
 * e.g. team arrival, opening ceremony, ...
 */
//TODO import
public class EventTimeSlot extends TimeSlot {

	private String name;

	public EventTimeSlot() {
		//default constructor for object mapping
	}

	public EventTimeSlot(final String name, final LocalTime time, final TimeMode timeMode) {
		super(time, timeMode);
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}
}

