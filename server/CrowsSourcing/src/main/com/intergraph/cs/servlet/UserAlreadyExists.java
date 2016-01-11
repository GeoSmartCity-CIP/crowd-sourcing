package com.intergraph.cs.servlet;

public class UserAlreadyExists extends CrowdSourcingException {
	public UserAlreadyExists(String reason) {
		super(reason);
	}

}
