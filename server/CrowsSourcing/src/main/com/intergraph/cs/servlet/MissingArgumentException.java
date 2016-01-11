package com.intergraph.cs.servlet;

public class MissingArgumentException extends CrowdSourcingException {
	public MissingArgumentException(String reason) {
		super(reason);
	}
}
