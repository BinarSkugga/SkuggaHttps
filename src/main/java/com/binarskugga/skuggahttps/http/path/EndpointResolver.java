package com.binarskugga.skuggahttps.http.path;

import com.google.common.base.*;
import com.google.common.cache.*;
import com.google.common.collect.*;
import com.google.common.hash.*;
import lombok.*;
import com.binarskugga.skuggahttps.*;
import com.binarskugga.skuggahttps.annotation.*;
import com.binarskugga.skuggahttps.http.*;
import com.binarskugga.skuggahttps.http.api.*;
import com.binarskugga.skuggahttps.utils.*;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;

public class EndpointResolver {

	private static PropertiesConfiguration configuration = HttpConfigProvider.get();
	private AbstractHttpExchangeHandler exchangeHandler;
	@Getter private List<Endpoint> endpoints;

	private Cache<String, Endpoint> routingCache;
	private BloomFilter<String> routingCacheFilter;

	public EndpointResolver(AbstractHttpExchangeHandler exchangeHandler, Collection<Class<? extends AbstractController>> controllers) {
		this.endpoints = controllers.stream().filter(controller -> controller.isAnnotationPresent(Controller.class))
				.map(controller -> Lists.newArrayList(controller.getDeclaredMethods()))
				.flatMap(Collection::stream)
				.filter(method ->  method.isAnnotationPresent(Get.class) || method.isAnnotationPresent(Post.class))
				.map(method -> {
					Endpoint endpoint = new Endpoint();
					endpoint.setType(method.getAnnotation(Get.class) == null ? EndpointType.POST : EndpointType.GET);

					endpoint.setRoute(this.sanitizePath(endpoint.getType().equals(EndpointType.GET) ? method.getAnnotation(Get.class).value() : method.getAnnotation(Post.class).value()));
					if(endpoint.getRoute() == null || endpoint.getRoute().equals(""))
						endpoint.setRoute(this.sanitizePath(method.getName().replaceAll("_", "/")));

					endpoint.setFullRoute(this.sanitizePath(configuration.getString("server.root") +
							method.getDeclaringClass().getAnnotation(Controller.class).value() + "/" + endpoint.getRoute()));
					endpoint.setAction(method);
					return endpoint;
				}).collect(Collectors.toList());

		this.exchangeHandler = exchangeHandler;
		this.routingCache = CacheBuilder.newBuilder().maximumSize(1000).expireAfterWrite(1, TimeUnit.HOURS).build();
		this.routingCacheFilter = BloomFilter.create(Funnels.stringFunnel(Charsets.UTF_8), 1000);
	}

	public Endpoint getEndpoint(String search, EndpointType type) {
		String sanitizedSearch = this.sanitizePath(search);
		Endpoint found = null;
		if(this.routingCacheFilter.mightContain(sanitizedSearch)) {
			found = this.routingCache.getIfPresent(sanitizedSearch);
		}
		if(found == null) {
			for(Endpoint entry : this.endpoints) {
				if(!entry.getType().equals(type)) continue;
				if(this.matchEndpoint(entry.getFullRoute(), sanitizedSearch)) {
					this.routingCacheFilter.put(sanitizedSearch);
					this.routingCache.put(sanitizedSearch, entry);
					found = entry;
					break;
				}
			}
		}
		return found;
	}

	public String sanitizePath(String path) {
		if(path.endsWith("/")) {
			path = path.substring(0, path.length() - 1);
		}
		if(path.startsWith("/")) {
			path = path.substring(1);
		}
		return path;
	}

	public boolean matchEndpoint(String endpoint, String request) {
		String[] brokenEndpoint = endpoint.split("/");
		String[] brokenRequest = request.split("/");

		if(brokenEndpoint.length != brokenRequest.length) {
			return false;
		}

		for(int i = 0; i < brokenEndpoint.length; i++) {
			String part = brokenEndpoint[i];
			if(!part.startsWith("{")) {
				if(!part.equals(brokenRequest[i])) {
					return false;
				}
			}
		}
		return true;
	}

	public Map<Parameter, Object> getArguments(HttpSession session, String endpoint) {
		String[] brokenEndpoint = this.sanitizePath(endpoint).split("/");
		String[] brokenRequest = this.sanitizePath(session.getEndpoint().getFullRoute()).split("/");
		List<Object> args = new ArrayList<>();

		for(int i = 0; i < brokenEndpoint.length; i++) {
			if(brokenRequest[i].startsWith("{")) {
				args.add(deduceType(brokenRequest[i], brokenEndpoint[i]));
			}
		}

		List<Object> fullArgs;
		if(session.getEndpoint().getType().equals(EndpointType.POST))
			fullArgs = Lists.asList(session.getBody(), args.toArray());
		else
			fullArgs = Lists.newArrayList(args);

		Parameter[] params = session.getEndpoint().getAction().getParameters();
		Map<Parameter, Object> mappedArgs = new HashMap<>();
		for(int i = 0; i < params.length; i++) {
			mappedArgs.put(params[i], fullArgs.get(i));
		}

		return mappedArgs;
	}

	public Object deduceType(String type, String argument) {
		switch(type) {
			case "{int}":
				return Integer.parseInt(argument);
			case "{id}":
				return this.exchangeHandler.createID(argument);
			case "{string}":
				return argument;
		}
		return argument;
	}

}