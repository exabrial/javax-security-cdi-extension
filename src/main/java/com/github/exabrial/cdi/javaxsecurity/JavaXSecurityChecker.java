package com.github.exabrial.cdi.javaxsecurity;

import java.io.IOException;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Default
@RequestScoped
public class JavaXSecurityChecker {
	@Inject
	private HttpServletRequest request;
	@Inject
	private HttpServletResponse response;
	private Boolean authenticated = null;

	public boolean isAuthenticated() {
		if (authenticated == null) {
			authenticated = checkAuthentication();
		}
		return authenticated;
	}

	public boolean isCallerInRole(String roleName) {
		return request.isUserInRole(roleName);
	}

	private boolean checkAuthentication() {
		try {
			return request.authenticate(response);
		} catch (IOException | ServletException e) {
			throw new RuntimeException(e);
		}
	}
}
