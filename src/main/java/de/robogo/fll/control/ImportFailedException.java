package de.robogo.fll.control;

public class ImportFailedException extends Exception {

	public ImportFailedException(String msg) {
		super(msg);
	}

	public ImportFailedException(Throwable cause) {
		super("An unexpected Error occurred. Please send a bug report to the developers: " + cause.getMessage(), cause);
	}

	public ImportFailedException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
