package com.uae.pass.api;

import retrofit2.Response;

public class UAEPassAPIException extends Exception {

	public transient Response errorResponse;

	public UAEPassAPIException(Response errorResponse) {
		this.errorResponse = errorResponse;
	}

	public UAEPassAPIException(Response errorResponse, String msg) {
		super(msg);
		this.errorResponse = errorResponse;
	}

	public UAEPassAPIException(Response errorResponse, String msg, Throwable cause) {
		super(msg, cause);
		this.errorResponse = errorResponse;
	}

	public UAEPassAPIException(Response errorResponse, Throwable cause) {
		super(cause);
		this.errorResponse = errorResponse;
	}

	public Response getErrorResponse() {
		return errorResponse;
	}
}
