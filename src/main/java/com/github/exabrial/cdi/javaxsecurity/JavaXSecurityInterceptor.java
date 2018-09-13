package com.github.exabrial.cdi.javaxsecurity;

import java.util.Optional;
import java.util.stream.Stream;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

import org.slf4j.Logger;

public class JavaXSecurityInterceptor {
	@Inject
	private Instance<Logger> logInstance;
	@Inject
	@SkipSecurity
	private Instance<Boolean> skipSecurityInstance;
	@Inject
	private JavaXSecurityChecker javaXSecurityChecker;
	@Inject
	private JavaXSecurityFailureHandler handler;

	@AroundInvoke
	public Object intercept(InvocationContext ctx) throws Exception {
		boolean skipSecurity = Optional.ofNullable(skipSecurityInstance.get()).orElse(Boolean.FALSE);
		if (!skipSecurity) {
			if (!javaXSecurityChecker.isAuthenticated()) {
				RolesAllowed rolesAllowed = ctx.getMethod().getAnnotation(RolesAllowed.class);
				if (rolesAllowed == null) {
					rolesAllowed = ctx.getTarget().getClass().getAnnotation(RolesAllowed.class);
				}
				if (rolesAllowed != null) {
					Stream.of(rolesAllowed.value()).forEach(roleName -> {
						if (!javaXSecurityChecker.isCallerInRole(roleName)) {
							handler.authorizationFailure(ctx.getTarget().getClass(), ctx.getMethod(), roleName);
						}
					});
				}
			} else {
				handler.authenticationFailure();
			}
		} else {
			Optional.ofNullable(logInstance.get()).ifPresent(log -> log.warn("check() Security disabled; skipping check for:{}:{}",
					ctx.getTarget().getClass().getName(), ctx.getMethod().getName()));
		}
		return ctx.proceed();
	}
}
