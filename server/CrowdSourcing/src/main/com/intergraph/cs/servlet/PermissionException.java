package com.intergraph.cs.servlet;

public class PermissionException extends CrowdSourcingException {
	private static final long serialVersionUID = -7950040849778067491L;

	public PermissionException(String reason) {
		super(reason);
	}
}
