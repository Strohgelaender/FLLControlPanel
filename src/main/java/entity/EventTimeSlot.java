package entity;

import java.time.LocalDateTime;

public class EventTimeSlot extends TimeSlot {

	private String name;
	private LocalDateTime time;

	public EventTimeSlot(final String name, final LocalDateTime time) {
		this.name = name;
		this.time = time;
	}
}
