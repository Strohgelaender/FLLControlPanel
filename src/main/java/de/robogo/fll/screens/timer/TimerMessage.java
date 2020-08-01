package de.robogo.fll.screens.timer;

public class TimerMessage {

	/**
	 * Time in milliseconds.
	 */
	private final long time;
	/**
	 * whether or not the Timer is a Game-Timer or just a "regular" countdown
	 * game timer uses other colors (default fade green -> red)
	 * as the regular countdown (default: blue).
	 */
	private final boolean game;
	/**
	 * If true the counter directly starts;
	 * If false it only sets the value.
	 */
	private final boolean start;

	public TimerMessage(final long time, final boolean game, final boolean start) {
		this.time = time;
		this.game = game;
		this.start = start;
	}

	public long getTime() {
		return time;
	}

	public boolean isGame() {
		return game;
	}

	public boolean isStart() {
		return start;
	}
}
