package com.binarskugga;

import com.binarskugga.impl.ObjectifyConnector;
import com.binarskugga.impl.ObjectifyHandler;
import com.binarskugga.skugga.ServerProperties;
import com.binarskugga.skugga.SkuggaHttp;
import com.binarskugga.skugga.SkuggaHttpHandler;
import com.binarskugga.skugga.util.ResourceUtils;
import com.google.auth.oauth2.GoogleCredentials;

import java.util.logging.LogManager;

public class Test {

	public static void main(String[] args) throws Exception {
		LogManager.getLogManager().readConfiguration(ResourceUtils.getLoggingProperties());
		ServerProperties.load(ResourceUtils.getServerProperties());

		ObjectifyConnector connector = new ObjectifyConnector("sru-api",
				GoogleCredentials.fromStream(ResourceUtils.getCompiledResource("google-account.json")));
		connector.connect(ServerProperties.getModelPackage());

		SkuggaHttpHandler handler = new SkuggaHttpHandler();
		handler.append(new ObjectifyHandler());
		SkuggaHttp server = new SkuggaHttp(handler);
		server.start();
	}

}
