package com.binarskugga.skuggahttps.api.exception.http;

import com.binarskugga.skuggahttps.api.enums.HttpStatus;

public class NotImplementedException extends HttpException {

	public NotImplementedException() {
		super(HttpStatus.NOT_IMPLEMENTED, HttpStatus.NOT_IMPLEMENTED.getCaption());
	}

	public NotImplementedException(String message) {
		super(HttpStatus.NOT_IMPLEMENTED, message);
	}

}
