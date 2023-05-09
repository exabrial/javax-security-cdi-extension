package com.github.exabrial.cdi.javaxsecurity;

import java.lang.reflect.Method;

public interface JavaXSecurityFailureHandler {
	void authenticationFailure() throws RuntimeException;

	void authorizationFailure(Class<? extends Object> targetClass, Method targetMethodName, String[] rolenames) throws RuntimeException;
}
