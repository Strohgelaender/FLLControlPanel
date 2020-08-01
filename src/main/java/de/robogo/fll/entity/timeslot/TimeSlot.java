package de.robogo.fll.entity.timeslot;

import java.io.Serializable;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import de.robogo.fll.entity.TimeMode;

/**
 * Abstract class representing any TimeSlot
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@JsonSubTypes({
		@JsonSubTypes.Type(value = JuryTimeSlot.class, name = "juryTimeSlot"),
		@JsonSubTypes.Type(value = RobotGameTimeSlot.class, name = "robotGameTimeSlot"),
		@JsonSubTypes.Type(value = EventTimeSlot.class, name = "eventTimeSlot"),
		@JsonSubTypes.Type(value = RobotGamePauseTimeSlot.class, name = "robotGamePauseTimeSlot"),
		@JsonSubTypes.Type(value = JuryPauseTimeSlot.class, name = "juryPauseTimeSlot")
})
public abstract class TimeSlot implements Serializable {

	//TODO chage to DayTime for multiple Day events
	@JsonFormat(pattern = "HH:mm")
	private LocalTime time;
	private TimeMode timeMode;

	protected TimeSlot() {
		//default constructor for object mapping
	}

	protected TimeSlot(LocalTime time, TimeMode timeMode) {
		this.time = time;
		this.timeMode = timeMode;
	}

	public LocalTime getTime() {
		return time;
	}

	public void setTime(final LocalTime time) {
		this.time = time;
	}

	public TimeMode getTimeMode() {
		return timeMode;
	}

	public void setTimeMode(final TimeMode timeMode) {
		this.timeMode = timeMode;
	}
}
