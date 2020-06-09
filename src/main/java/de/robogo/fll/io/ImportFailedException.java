package de.robogo.fll.io;

public class ImportFailedException extends Exception {

	ImportFailedException(String msg) {
		super(msg);
	}

	ImportFailedException(Throwable cause) {
		super("An unexpected Error occurred. Please send a bug report to the developers: " + cause.getMessage(), cause);
	}

	ImportFailedException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
