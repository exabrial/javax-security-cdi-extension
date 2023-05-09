package com.github.exabrial.cdi.javaxsecurity;

import java.lang.reflect.Method;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class DefaultJavaXSecurityFailureHandler implements JavaXSecurityFailureHandler {
	private final Logger log = LoggerFactory.getLogger(getClass());

	@Inject
	@JavaxSecurityHttpRequest
	private HttpServletRequest request;

	@Override
	public void authenticationFailure() {
		throw new WebApplicationException(Response.Status.UNAUTHORIZED);
	}

	@Override
	public void authorizationFailure(final Class<? extends Object> targetClass, final Method targetMethod, final String[] roleNames) {
		log.warn("authorizationFailure() user:{} is authenticated, but doesn't have one roles:{} required for invoking:{}:{}",
				request.getRemoteUser(), roleNames, targetClass.getName(), targetMethod.getName());
		throw new WebApplicationException(Response.Status.UNAUTHORIZED);
	}
}
