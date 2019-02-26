package com.binarskugga.skuggahttps.api.impl.parse.parameter;

import com.binarskugga.skuggahttps.api.ParameterParser;
import org.bson.types.ObjectId;

import java.lang.reflect.Parameter;

public class ObjectIdParser implements ParameterParser<ObjectId, String> {

	@Override
	public ObjectId parse(Parameter parameter, String argument) {
		return new ObjectId(argument);
	}

	@Override
	public boolean predicate(Parameter c) {
		return ObjectId.class.equals(c.getType());
	}

}