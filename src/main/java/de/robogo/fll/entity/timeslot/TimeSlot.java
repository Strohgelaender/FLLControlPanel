package de.robogo.fll.entity.timeslot;

import java.io.Serializable;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@JsonSubTypes({
		@JsonSubTypes.Type(value = JuryTimeSlot.class, name = "juryTimeSlot"),
		@JsonSubTypes.Type(value = RobotGameTimeSlot.class, name = "robotGameTimeSlot"),
		@JsonSubTypes.Type(value = EventTimeSlot.class, name = "eventTimeSlot"),
		@JsonSubTypes.Type(value = RobotGamePauseTimeSlot.class, name = "robotGamePauseTimeSlot"),
		@JsonSubTypes.Type(value = JuryPauseTimeSlot.class, name = "juryPauseTimeSlot")
})
public abstract class TimeSlot implements Serializable {

	private LocalTime time;

	protected TimeSlot() {
		//default constructor for object mapping
	}

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
