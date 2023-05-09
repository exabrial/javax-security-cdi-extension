package com.github.exabrial.cdi.javaxsecurity;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;

@WebFilter(
		asyncSupported = true,
		urlPatterns = "/*",
		dispatcherTypes = { DispatcherType.ASYNC, DispatcherType.ERROR, DispatcherType.FORWARD, DispatcherType.INCLUDE,
				DispatcherType.REQUEST })
public class JavaxSecurityRequestFilter implements Filter {
	@Inject
	private HttpServletRequestHolder holder;

	@Override
	public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
			throws IOException, ServletException {
		holder.set((HttpServletRequest) request);
		chain.doFilter(request, response);
	}
}
