package com.github.exabrial.cdi.javaxsecurity;

import java.lang.reflect.Modifier;
import java.util.regex.Pattern;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Asynchronous;
import javax.ejb.Local;
import javax.ejb.LocalBean;
import javax.ejb.Lock;
import javax.ejb.MessageDriven;
import javax.ejb.Remote;
import javax.ejb.Singleton;
import javax.ejb.Stateful;
import javax.ejb.Stateless;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.configurator.AnnotatedMethodConfigurator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JavaXSecurityCDIPortableExtension implements Extension {
	private final Logger log = LoggerFactory.getLogger(getClass());
	private final Pattern securePattern;
	private final Pattern ignorePattern;

	public JavaXSecurityCDIPortableExtension() {
		final String securePatternString = System.getProperty("JavaXSecurityCDIPortableExtension.securePattern");
		final String ignorePatternString = System.getProperty("JavaXSecurityCDIPortableExtension.ignorePattern");

		if (securePatternString != null) {
			securePattern = Pattern.compile(securePatternString);
		} else {
			securePattern = null;
		}

		if (ignorePatternString != null) {
			ignorePattern = Pattern.compile(ignorePatternString);
		} else {
			ignorePattern = null;
		}
	}

	public <T> void processAnnotatedType(@Observes final ProcessAnnotatedType<T> processAnnotatedType) {
		final AnnotatedType<T> annotatedType = processAnnotatedType.getAnnotatedType();
		// TODO The goal here is to skip adding interceptors to any EJB class, since
		// RolesAllowed will work there. This check isn't that great, but it's a start
		// :/
		if (accept(annotatedType.getJavaClass().getName()) && !annotatedType.isAnnotationPresent(Singleton.class)
				&& !annotatedType.isAnnotationPresent(MessageDriven.class) && !annotatedType.isAnnotationPresent(Stateless.class)
				&& !annotatedType.isAnnotationPresent(Stateful.class) && !annotatedType.isAnnotationPresent(Remote.class)
				&& !annotatedType.isAnnotationPresent(Local.class) && !annotatedType.isAnnotationPresent(Lock.class)
				&& !annotatedType.isAnnotationPresent(Asynchronous.class) && !annotatedType.isAnnotationPresent(LocalBean.class)) {
			if (annotatedType.getAnnotation(RolesAllowed.class) != null) {
				log.info("processAnnotatedType() enforcing:{} on:{}", annotatedType.getAnnotation(RolesAllowed.class),
						annotatedType.getJavaClass());
				processAnnotatedType.configureAnnotatedType().add(EnforceRolesAllowed.LITERAL);
			}
			addInterceptorToApplicableMethods(processAnnotatedType);
		}
	}

	protected boolean accept(final String className) {
		boolean accept = true;
		if (securePattern != null && !securePattern.matcher(className).matches()) {
			accept = false;
		}
		if (accept && ignorePattern != null && ignorePattern.matcher(className).matches()) {
			accept = false;
		}
		return accept;
	}

	private <T> void addInterceptorToApplicableMethods(final ProcessAnnotatedType<T> processAnnotatedType) {
		final AnnotatedType<T> annotatedType = processAnnotatedType.getAnnotatedType();
		final Class<?> clazz = annotatedType.getJavaClass();
		processAnnotatedType.configureAnnotatedType()
				.filterMethods(
						(final AnnotatedMethod<? super T> filterCandidate) -> filterCandidate.getJavaMember().getDeclaringClass().equals(clazz)
								&& !Modifier.isPrivate(filterCandidate.getJavaMember().getModifiers())
								&& filterCandidate.isAnnotationPresent(RolesAllowed.class))
				.forEach((final AnnotatedMethodConfigurator<? super T> configurator) -> {
					log.info("processAnnotatedType() enforcing:{} on:{}", configurator.getAnnotated().getAnnotation(RolesAllowed.class),
							configurator.getAnnotated().getJavaMember());
					configurator.add(EnforceRolesAllowed.LITERAL);
				});
	}
}
