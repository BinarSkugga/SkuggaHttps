package com.binarskugga.skuggahttps.impl;

import com.binarskugga.skuggahttps.api.AuthentifiableEntity;
import com.binarskugga.skuggahttps.api.Token;
import com.binarskugga.skuggahttps.api.annotation.Controller;
import com.binarskugga.skuggahttps.api.annotation.Post;
import com.binarskugga.skuggahttps.api.exception.InvalidArgumentException;
import com.binarskugga.skuggahttps.api.exception.auth.InvalidTokenException;
import com.binarskugga.skuggahttps.api.exception.auth.LoginException;
import com.binarskugga.skuggahttps.api.exception.auth.NoAuthException;
import com.binarskugga.skuggahttps.api.exception.entity.EntityInexistantException;
import com.binarskugga.skuggahttps.api.impl.endpoint.AuthController;
import com.binarskugga.skuggahttps.util.ReflectionUtils;

@Controller("token")
public class TokenController extends AuthController {

	@SuppressWarnings("unchecked")
	@Post
	public String ltt(Login login) {
		if(this.getAuthRepository() == null)
			throw new NoAuthException();

		if (login == null)
			throw new InvalidArgumentException();

		AuthentifiableEntity entity = (AuthentifiableEntity) this.getAuthRepository().load("authentifier", login.getAuthentifier());

		if (entity == null)
			throw new EntityInexistantException();
		if (!entity.validatePassword(login.getPassword()))
			throw new LoginException();

		Token token = ReflectionUtils.constructOrNull(this.getTokenClass());
		token.setAuthentifier(entity.getAuthentifier());
		token.setLTT(true);
		return token.generate();
	}

	@SuppressWarnings("unchecked")
	@Post
	public String stt(String ltt) {
		if(this.getAuthRepository() == null)
			throw new NoAuthException();

		Token token = ReflectionUtils.constructOrNull(this.getTokenClass());
		if (token == null)
			throw new InvalidTokenException();

		token.parse(ltt);
		if (!token.isLTT())
			throw new InvalidTokenException();

		AuthentifiableEntity entity = (AuthentifiableEntity) this.getAuthRepository().load("authentifier", token.getAuthentifier());
		if (entity == null)
			throw new EntityInexistantException();

		token.setAuthentifier(entity.getAuthentifier());
		token.setLTT(false);
		return token.generate();
	}

}