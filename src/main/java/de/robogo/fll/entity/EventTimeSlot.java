package de.robogo.fll.entity;

import java.time.LocalTime;

//TODO this is currently completly unsupported
public class EventTimeSlot extends TimeSlot {

	private String name;

	public EventTimeSlot() {
		//default constructor for object mapping
	}

	public EventTimeSlot(final String name, final LocalTime time) {
		super(time);
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}
}

