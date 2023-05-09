package com.github.exabrial.cdi.javaxsecurity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.enterprise.util.AnnotationLiteral;
import javax.interceptor.InterceptorBinding;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
@InterceptorBinding
@Inherited
public @interface EnforceRolesAllowed {
	AnnotationLiteral<EnforceRolesAllowed> LITERAL = new AnnotationLiteral<EnforceRolesAllowed>() {
		private static final long serialVersionUID = 1L;
	};
}
