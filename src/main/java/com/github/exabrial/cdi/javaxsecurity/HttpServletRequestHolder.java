package com.github.exabrial.cdi.javaxsecurity;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.servlet.http.HttpServletRequest;

@RequestScoped
class HttpServletRequestHolder {
	private HttpServletRequest request;

	void set(final HttpServletRequest request) {
		this.request = request;
	}

	@Produces
	@JavaxSecurityHttpRequest
	HttpServletRequest produce() {
		return request;
	}
}
