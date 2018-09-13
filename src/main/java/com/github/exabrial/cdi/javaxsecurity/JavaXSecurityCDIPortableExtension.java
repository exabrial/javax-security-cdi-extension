package com.github.exabrial.cdi.javaxsecurity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.interceptor.Interceptors;

import org.apache.deltaspike.core.util.metadata.AnnotationInstanceProvider;
import org.apache.deltaspike.core.util.metadata.builder.AnnotatedTypeBuilder;

public class JavaXSecurityCDIPortableExtension implements Extension {
	<T> void processAnnotatedType(@Observes ProcessAnnotatedType<T> pat) {
		AnnotatedType<T> annotatedType = pat.getAnnotatedType();
		AnnotatedTypeBuilder<T> builder = new AnnotatedTypeBuilder<T>().readFromType(annotatedType);
		// TODO The goal here is to skip adding interceptors to any EJB class, since
		// RolesAllowed will work there. This check isn't that great, but it's a start
		// :/
		if (!annotatedType.isAnnotationPresent(Singleton.class) && !annotatedType.isAnnotationPresent(MessageDriven.class)
				&& !annotatedType.isAnnotationPresent(Stateless.class) && !annotatedType.isAnnotationPresent(Stateful.class)
				&& !annotatedType.isAnnotationPresent(Remote.class) && !annotatedType.isAnnotationPresent(Local.class)
				&& !annotatedType.isAnnotationPresent(Lock.class) && !annotatedType.isAnnotationPresent(Asynchronous.class)
				&& !annotatedType.isAnnotationPresent(LocalBean.class)) {
			addInterceptorToClass(annotatedType, builder);
			addInterceptorToMethods(annotatedType, builder);
			pat.setAnnotatedType(builder.create());
		}
	}

	private <T> void addInterceptorToClass(AnnotatedType<T> annotatedType, AnnotatedTypeBuilder<T> builder) {
		RolesAllowed rolesAllowed = annotatedType.getAnnotation(RolesAllowed.class);
		if (rolesAllowed != null) {
			Interceptors toAdd = annotatedType.getAnnotation(Interceptors.class);
			if (toAdd == null) {
				toAdd = interceptors(new Class<?>[0]);
			} else {
				builder.removeFromClass(Interceptors.class);
				toAdd = appendToInterceptors(toAdd);
			}
			builder.addToClass(toAdd);
		}
	}

	private <T> void addInterceptorToMethods(AnnotatedType<T> annotatedType, AnnotatedTypeBuilder<T> builder) {
		annotatedType.getMethods().forEach(method -> {
			RolesAllowed rolesAllowed = method.getAnnotation(RolesAllowed.class);
			if (rolesAllowed != null) {
				Interceptors toAdd = method.getAnnotation(Interceptors.class);
				if (toAdd == null) {
					toAdd = interceptors(new Class<?>[0]);
				} else {
					builder.removeFromMethod(method, Interceptors.class);
					toAdd = appendToInterceptors(toAdd);
				}
				builder.addToMethod(method, toAdd);
			}
		});
	}

	private Interceptors appendToInterceptors(Interceptors toAdd) {
		Class<?>[] oldValue = toAdd.value();
		return interceptors(oldValue);
	}

	private Interceptors interceptors(Class<?>[] value) {
		Map<String, Class<?>[]> arguments = new HashMap<>();
		List<Class<?>> classes = Stream.of(value).collect(Collectors.toList());
		classes.add(JavaXSecurityInterceptor.class);
		arguments.put("value", classes.toArray(new Class<?>[classes.size()]));
		return AnnotationInstanceProvider.of(Interceptors.class, arguments);
	}
}
