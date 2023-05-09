package com.github.exabrial.cdi.javaxsecurity;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

@RequestScoped
public class JavaXSecurityChecker {
	@Inject
	@JavaxSecurityHttpRequest
	private HttpServletRequest request;

	public boolean isCallerInRole(final String roleName) {
		return request.isUserInRole(roleName);
	}

	public boolean isAuthenticated() {
		return request.getUserPrincipal() != null;
	}
}
