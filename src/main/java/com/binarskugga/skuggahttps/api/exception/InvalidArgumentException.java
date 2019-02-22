package com.binarskugga.skuggahttps.api.exception;

import com.binarskugga.skuggahttps.api.exception.http.BadRequestException;

public class InvalidArgumentException extends BadRequestException {

	public InvalidArgumentException(String message) {
		super(message);
	}

	public InvalidArgumentException() {
		super("One or more of the arguments passed are invalid are using the wrong format.");
	}

}
