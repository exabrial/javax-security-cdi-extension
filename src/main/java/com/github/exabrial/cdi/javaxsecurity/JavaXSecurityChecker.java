package com.github.exabrial.cdi.javaxsecurity;

import java.io.IOException;
import java.util.Base64;
import java.util.Optional;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;

@RequestScoped
public class JavaXSecurityChecker {
	@Inject
	private Instance<Logger> logInstance;
	@Inject
	@SkipSecurity
	private Instance<Boolean> skipSecurityInstance;
	@Context
	private HttpServletRequest request;
	@Context
	private HttpServletResponse response;
	private boolean authBeenAttempted = false;

	public void check(String roleName) {
		Optional.ofNullable(logInstance.get()).ifPresent(log -> log.trace("check() roleName:{}", roleName));
		boolean skipSecurity = Optional.ofNullable(skipSecurityInstance.get()).orElse(Boolean.FALSE);
		if (!skipSecurity) {
			if (!authBeenAttempted) {
				authenticate();
			} else if (!request.isUserInRole(roleName)) {
				throw new WebApplicationException(Response.Status.UNAUTHORIZED);
			}
		} else {
			Optional.ofNullable(logInstance.get()).ifPresent(log -> log.warn("check() Security disabled; skipping check for roleName:{}", roleName));
		}
	}

	public boolean authenticate() {
		authBeenAttempted = true;
		try {
			final boolean authenticated = request.authenticate(response);
			if (!authenticated) {
				Optional.ofNullable(logInstance.get()).ifPresent(log -> {
					String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
					String decoded;
					try {
						final String base64Encoded = authHeader.substring(authHeader.indexOf(" ") + 1, authHeader.length());
						decoded = new String(Base64.getDecoder().decode(base64Encoded));
					} catch (Exception e) {
						decoded = null;
					}
					log.warn("authenticate() failure remoteAddr:{} authorization header:{}[{}]", request.getRemoteAddr(), authHeader, decoded);
				});
			}
			return authenticated;
		} catch (IOException | ServletException e) {
			throw new RuntimeException(e);
		}
	}
}
