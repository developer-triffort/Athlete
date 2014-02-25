package com.athlete.exception;

/**
 * @author edBaev
 * */
public class BaseException extends Exception {
	private static final long serialVersionUID = -4771241271898361734L;
	private int error_code;
	private String error_msg;

	public BaseException(String error_msg, int error_code) {
		this.error_msg = error_msg;
		this.error_code = error_code;
	}

	public BaseException(Throwable throwable, String error_msg, int error_code) {
		super(throwable);
		this.error_msg = error_msg;
		this.error_code = error_code;
	}

	public int getError_code() {
		return error_code;
	}

	public String getError_msg() {
		return error_msg;
	}
}
