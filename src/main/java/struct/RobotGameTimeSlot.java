package struct;

import java.time.LocalDateTime;

import teams.Team;

public class RobotGameTimeSlot extends TimeSlot {

	private final Team teamA;
	private final Team teamB;
	private final Table tableA;
	private final Table tableB;
	private final LocalDateTime time;
	private Status status;

	public RobotGameTimeSlot(final Team teamA, final Team teamB, final Table tableA, final Table tableB, final LocalDateTime time) {
		this.teamA = teamA;
		this.teamB = teamB;
		this.tableA = tableA;
		this.tableB = tableB;
		this.time = time;
		status = Status.Scheduled;
	}

	public enum Status {
		Scheduled, Preparation, Running, Evaluation
	}
}
