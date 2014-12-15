package de.kune.server.filter;

import java.io.IOException;
import java.util.Date;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class GwtCacheControlFilter implements Filter {

	public void destroy() {
	}

	public void init(FilterConfig config) throws ServletException {
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain filterChain) throws IOException, ServletException {

		HttpServletRequest httpRequest = (HttpServletRequest) request;
		String requestURI = httpRequest.getRequestURI();

		if (requestURI.contains(".nocache.")) {
			HttpServletResponse httpResponse = (HttpServletResponse) response;
			httpResponse.setDateHeader("Date", now());
			httpResponse.setDateHeader("Expires", yesterday());
			httpResponse.setHeader("Pragma", "no-cache");
			httpResponse.setHeader("Cache-control",
					"no-cache, no-store, must-revalidate");
		}

		filterChain.doFilter(request, response);
	}

	private long yesterday() {
		return now() - 1000 * 60 * 60 * 24;
	}

	private long now() {
		return new Date().getTime();
	}
}