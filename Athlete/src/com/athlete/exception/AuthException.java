package com.athlete.exception;

/**
 * @author edBaev
 * */
public class AuthException extends Exception {
	private static final long serialVersionUID = 2909951896129204799L;

	private String error;
	private String error_description;

	public AuthException(Throwable throwable, String error_description,
			String error) {
		super(throwable);
		this.error = error;
		this.error_description = error_description;
	}

	public AuthException(String error_description, String error) {
		super(error_description);
		this.error = error;
		this.error_description = error_description;
	}

	public AuthException(String message, Throwable throwable,
			String error_description, String error) {
		super(message, throwable);
		this.error = error;
		this.error_description = error_description;
	}

	public String getError() {
		return error;
	}

	public String getError_description() {
		return error_description;
	}
}
