package com.binarskugga.impl;

import com.binarskugga.skuggahttps.api.BaseEntity;
import com.binarskugga.skuggahttps.api.DataConnector;
import com.binarskugga.skuggahttps.util.ReflectionUtils;
import com.google.auth.Credentials;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.common.flogger.FluentLogger;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.annotation.Entity;
import org.reflections.Reflections;

import java.util.stream.Collectors;

public class ObjectifyConnector implements DataConnector<Datastore> {

	private static final FluentLogger logger = FluentLogger.forEnclosingClass();
	private String projectId;
	private Credentials credentials;

	public ObjectifyConnector(String projectId, Credentials credentials) {
		this.projectId = projectId;
		this.credentials = credentials;
	}

	@Override
	public Datastore connect(String modelPackage) {
		Datastore store = DatastoreOptions.newBuilder()
				.setProjectId(this.projectId).setCredentials(this.credentials)
				.build().getService();
		ObjectifyService.init(new ObjectifyFactory(store));

		Reflections reflections = new Reflections(modelPackage);
		reflections.getSubTypesOf(BaseEntity.class).stream()
				.filter(c -> ReflectionUtils.getClassAnnotationOrNull(c, Entity.class) != null)
				.collect(Collectors.toList()).forEach(c -> {
			ObjectifyService.register(c);
			logger.atFine().log("Entity class " + c.getSimpleName() + " has been registered !");
		});
		return null;
	}

}
