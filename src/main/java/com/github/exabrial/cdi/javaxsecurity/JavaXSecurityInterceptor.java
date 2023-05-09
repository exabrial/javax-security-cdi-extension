package com.github.exabrial.cdi.javaxsecurity;

import java.io.Serializable;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.annotation.Priority;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Interceptor
@EnforceRolesAllowed
@Priority(Interceptor.Priority.LIBRARY_BEFORE - 10)
public class JavaXSecurityInterceptor implements Serializable {
	private static final long serialVersionUID = 1L;
	private final Logger log = LoggerFactory.getLogger(getClass());

	@Inject
	@SkipSecurity
	private Instance<Boolean> skipSecurityInstance;
	private boolean skipSecurity;

	@Inject
	private JavaXSecurityChecker javaXSecurityChecker;
	@Inject
	private JavaXSecurityFailureHandler handler;

	@PostConstruct
	void postConstruct() {
		skipSecurity = !skipSecurityInstance.isResolvable() ? false : skipSecurityInstance.get();
	}

	@AroundInvoke
	public Object intercept(final InvocationContext ctx) throws Exception {
		if (!skipSecurity) {
			if (javaXSecurityChecker.isAuthenticated()) {
				RolesAllowed rolesAllowedAnnotation = ctx.getMethod().getAnnotation(RolesAllowed.class);
				if (rolesAllowedAnnotation == null) {
					rolesAllowedAnnotation = ctx.getTarget().getClass().getAnnotation(RolesAllowed.class);
				}
				if (rolesAllowedAnnotation != null) {
					final boolean anyMatch = Stream.of(rolesAllowedAnnotation.value())
							.anyMatch((final String roleName) -> javaXSecurityChecker.isCallerInRole(roleName));
					if (!anyMatch) {
						handler.authorizationFailure(ctx.getTarget().getClass(), ctx.getMethod(), rolesAllowedAnnotation.value());
					}
				}
			} else {
				handler.authenticationFailure();
			}
		} else {
			log.warn("check() Security disabled; skipping check at:{}:{}()", ctx.getTarget().getClass().getName(),
					ctx.getMethod().getName());
		}
		return ctx.proceed();
	}
}
