package com.github.exabrial.cdi.javaxsecurity;

import java.util.Optional;
import java.util.stream.Stream;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

public class JavaXSecurityInterceptor {
	@Inject
	@SkipSecurity
	private Instance<Boolean> skipSecurityInstance;
	@Inject
	private JavaXSecurityChecker javaXSecurityChecker;

	@AroundInvoke
	public Object intercept(InvocationContext ctx) throws Exception {
		boolean skipSecurity = Optional.ofNullable(skipSecurityInstance.get()).orElse(Boolean.FALSE);
		if (!skipSecurity) {
			RolesAllowed rolesAllowed = ctx.getMethod().getAnnotation(RolesAllowed.class);
			if (rolesAllowed == null) {
				rolesAllowed = ctx.getTarget().getClass().getAnnotation(RolesAllowed.class);
			}
			if (rolesAllowed != null) {
				Stream.of(rolesAllowed.value()).forEach(roleName -> {
					javaXSecurityChecker.check(roleName);
				});
			}
		}
		return ctx.proceed();
	}
}
