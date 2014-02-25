package com.athlete.model;

/**
 * @author edBaev
 */
public class TaskResult<Result> {
	private String error;
	private String error_description;
	private Result result;
	private boolean isError;
	private Exception exception;
	private int typeFeed;

	public void setError(boolean isError, String error, String error_description) {
		this.isError = isError;
		this.error = error;
		this.error_description = error_description;
	}

	public void setError(boolean isError, String error,
			String error_description, Exception ex) {
		setError(isError, error, error_description);
		exception = ex;
	}

	public String getError() {
		return this.error;
	}

	public boolean isError() {
		return isError;
	}

	public void setResult(Result result) {
		this.result = result;
	}

	public Result getResult() {
		return this.result;
	}

	public String getError_description() {
		return error_description;
	}

	public Exception getException() {
		return exception;
	}

	public void setException(Exception exception) {
		this.exception = exception;
	}

	public int getTypeFeed() {
		return typeFeed;
	}

	public void setTypeFeed(int typeFeed) {
		this.typeFeed = typeFeed;
	}
}
