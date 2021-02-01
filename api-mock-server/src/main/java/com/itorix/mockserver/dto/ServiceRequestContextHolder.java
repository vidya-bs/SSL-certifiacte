package com.itorix.mockserver.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServiceRequestContextHolder {

	private static ThreadLocal<ServiceRequestContext> contextHolder = new ThreadLocal<>();

	public synchronized static void clearContext() {
		contextHolder.set(null);
	}

	public synchronized static ServiceRequestContext getContext() {
		if (contextHolder.get() == null) {
			ServiceRequestContext ctx = new ServiceRequestContext();
			contextHolder.set(ctx);
		}
		return contextHolder.get();
	}

	public synchronized static void setContext(ServiceRequestContext context) {
		contextHolder.set(context);
	}

}