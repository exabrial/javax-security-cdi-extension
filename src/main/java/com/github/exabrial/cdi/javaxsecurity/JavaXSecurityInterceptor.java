package com.github.exabrial.cdi.javaxsecurity;

import java.util.stream.Stream;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

public class JavaXSecurityInterceptor {
	@Inject
	private JavaXSecurityChecker javaXSecurityChecker;

	@AroundInvoke
	public Object intercept(InvocationContext ctx) throws Exception {
		RolesAllowed rolesAllowed = ctx.getMethod().getAnnotation(RolesAllowed.class);
		if (rolesAllowed == null) {
			rolesAllowed = ctx.getTarget().getClass().getAnnotation(RolesAllowed.class);
		}
		if (rolesAllowed != null) {
			String[] value = rolesAllowed.value();
			if (value != null) {
				Stream.of(value).forEach(roleName -> {
					javaXSecurityChecker.check(roleName);
				});
			}
		}
		return ctx.proceed();
	}
}
