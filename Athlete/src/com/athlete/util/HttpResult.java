package com.athlete.util;

/**
 * @author edBaev
 * */
public class HttpResult {
	private String errorMessage;
	private String response;
	private int resultCode;

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public String getErrorMessage() {
		return this.errorMessage;
	}

	public boolean isError() {
		return this.errorMessage != null;
	}

	public void setResponse(String result) {
		this.response = result;
	}

	public String getResponse() {
		return this.response;
	}

	public int getResponseCode() {
		return resultCode;
	}

	public void setResponseCode(int i) {
		resultCode = i;
	}
}
