package com.binarskugga.skuggahttps.api.impl.endpoint;

import com.binarskugga.skuggahttps.api.*;
import com.binarskugga.skuggahttps.api.annotation.*;
import com.binarskugga.skuggahttps.api.impl.*;
import com.binarskugga.skuggahttps.util.*;
import lombok.*;
import org.reflections.Reflections;

import java.util.stream.*;

public abstract class AuthController extends AbstractController {

	@Getter private DataRepository authRepository;
	@Getter private Class<? extends Token> tokenClass;

	@Override
	public AbstractController setSession(HttpSession session) {
		super.setSession(session);
		Reflections reflections = new Reflections(ServerProperties.getModelPackage());
		Class<? extends AuthentifiableEntity> authenticatorClass = reflections.getSubTypesOf(AuthentifiableEntity.class).stream()
				.filter(c -> ReflectionUtils.getClassAnnotationOrNull(c, Authenticator.class) != null)
				.collect(Collectors.toList()).get(0);
		if(authenticatorClass != null) {
			Class<? extends DataRepository> repositoryClass = ReflectionUtils.getClassAnnotationOrNull(authenticatorClass, Authenticator.class).repository();
			this.authRepository = ReflectionUtils.constructOrNull(repositoryClass, authenticatorClass);
			this.tokenClass = ServerProperties.getTokenClass();
		}
		return this;
	}

}