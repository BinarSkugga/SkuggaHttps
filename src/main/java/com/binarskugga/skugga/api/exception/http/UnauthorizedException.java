package com.binarskugga.skugga.api.exception.http;

import com.binarskugga.skugga.api.enums.HttpStatus;

public class UnauthorizedException extends HttpException {

	public UnauthorizedException() {
		super(HttpStatus.UNAUTHORIZED, HttpStatus.UNAUTHORIZED.getCaption());
	}

	public UnauthorizedException(String message) {
		super(HttpStatus.UNAUTHORIZED, message);
	}

}
