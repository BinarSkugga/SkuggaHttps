package com.binarskugga.skugga.api.impl.handler;

import com.binarskugga.primitiva.reflection.PrimitivaReflection;
import com.binarskugga.skugga.ServerProperties;
import com.binarskugga.skugga.api.RequestHandler;
import com.binarskugga.skugga.api.Token;
import com.binarskugga.skugga.api.annotation.Connected;
import com.binarskugga.skugga.api.annotation.NotConnected;
import com.binarskugga.skugga.api.exception.auth.InsufficientRoleException;
import com.binarskugga.skugga.api.exception.auth.InvalidTokenException;
import com.binarskugga.skugga.api.impl.endpoint.Endpoint;
import com.binarskugga.skugga.api.impl.endpoint.HttpSession;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import io.undertow.server.handlers.Cookie;
import io.undertow.server.handlers.CookieImpl;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class AuthHandler implements RequestHandler {

	@Override
	public boolean handle(HttpSession session) throws RuntimeException {
		Endpoint endpoint = session.getEndpoint();
		Class controller = endpoint.getController();
		if (PrimitivaReflection.getMethodAnnotationOrNull(endpoint.getAction(), NotConnected.class) != null)
			return true;

		Connected connected = PrimitivaReflection.getMethodAnnotationOrNull(endpoint.getAction(), Connected.class);
		if (connected == null) connected = PrimitivaReflection.getClassAnnotationOrNull(controller, Connected.class);
		if (connected == null) return true;

		List<String> roles = Arrays.stream(connected.roles()).map(String::toUpperCase).collect(Collectors.toList());

		Token token = null;
		if (session.getRequestCookies().containsKey("token")) {
			try {
				Cookie cookie = session.getRequestCookies().get("token");
				token = this.createToken(cookie);
			} catch (SignatureException | ExpiredJwtException | MalformedJwtException | UnsupportedJwtException e) {
				Cookie tokenCookie = new CookieImpl("token", "");
				tokenCookie.setExpires(new Date(new Date().getTime() - 1000));
				tokenCookie.setHttpOnly(true);
				tokenCookie.setPath("/");
				session.getExchange().setResponseCookie(tokenCookie);
				throw new InvalidTokenException();
			}
		}

		if (token == null)
			throw new InsufficientRoleException();

		if (token.isLTT())
			throw new InvalidTokenException();

		if (!roles.contains("*") && !roles.contains(token.getRole().name().toUpperCase()))
			throw new InsufficientRoleException();

		session.setToken(token);
		return true;
	}

	@SuppressWarnings("unchecked")
	private <T extends Token> T createToken(Cookie cookie) throws SignatureException, ExpiredJwtException, MalformedJwtException, UnsupportedJwtException {
		Token token = PrimitivaReflection.constructOrNull(ServerProperties.getTokenClass());
		if (token == null) return null;

		token.parse(cookie.getValue());
		return (T) token;
	}

}
