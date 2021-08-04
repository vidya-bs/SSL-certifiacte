package com.itorix.apiwiz.virtualization.model.metadata;

public class HttpRequest {
	private Headers[] headers;

	private Body body;

	private Path path;

	private QueryStringParameters[] queryStringParameters;

	private Method method;

	private Cookies[] cookies;

	public Headers[] getHeaders() {
		return headers;
	}

	public void setHeaders(Headers[] headers) {
		this.headers = headers;
	}

	public Body getBody() {
		return body;
	}

	public void setBody(Body body) {
		this.body = body;
	}

	public Path getPath() {
		return path;
	}

	public void setPath(Path path) {
		this.path = path;
	}

	public QueryStringParameters[] getQueryStringParameters() {
		return queryStringParameters;
	}

	public void setQueryStringParameters(QueryStringParameters[] queryStringParameters) {
		this.queryStringParameters = queryStringParameters;
	}

	public Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}

	public Cookies[] getCookies() {
		return cookies;
	}

	public void setCookies(Cookies[] cookies) {
		this.cookies = cookies;
	}

	@Override
	public String toString() {
		return "ClassPojo [headers = " + headers + ", body = " + body + ", path = " + path
				+ ", queryStringParameters = " + queryStringParameters + ", method = " + method + ", cookies = "
				+ cookies + "]";
	}
}
