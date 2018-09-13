package com.github.exabrial.cdi.javaxsecurity;

import java.io.IOException;

import javax.enterprise.context.RequestScoped;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

@RequestScoped
@Provider
public class JavaXSecurityChecker {
	@Context
	private HttpServletRequest request;
	@Context
	private HttpServletResponse response;
	private Boolean authenticated = null;

	public boolean authenticate() {
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
			if (request.getUserPrincipal() == null) {
				return request.authenticate(response);
			} else {
				return true;
			}
		} catch (IOException | ServletException e) {
			throw new RuntimeException(e);
		}
	}
}
