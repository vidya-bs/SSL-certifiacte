package com.itorix.hyggee.mockserver.matchers;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.itorix.hyggee.mockserver.client.serialization.ObjectMapperFactory;
import com.itorix.hyggee.mockserver.client.serialization.model.*;
import com.itorix.hyggee.mockserver.logging.MockServerLogger;
import com.itorix.hyggee.mockserver.mock.Expectation;
import com.itorix.hyggee.mockserver.model.*;

import org.springframework.cloud.sleuth.SpanAccessor;

import static com.itorix.hyggee.mockserver.character.Character.NEW_LINE;
import static com.itorix.hyggee.mockserver.log.model.MessageLogEntry.LogMessageType.EXPECTATION_MATCHED;
import static com.itorix.hyggee.mockserver.log.model.MessageLogEntry.LogMessageType.EXPECTATION_NOT_MATCHED;
import static com.itorix.hyggee.mockserver.model.NottableString.string;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 *   
 */
public class HttpRequestMatcher extends NotMatcher<HttpRequest> {

	private static final String[] excludedFields = {"mockServerLogger", "objectMapper"};
	private MockServerLogger mockServerLogger;
	private Expectation expectation;
	private HttpRequest httpRequest;
	private RegexStringMatcher methodMatcher = null;
	private RegexStringMatcher pathMatcher = null;
	private MultiValueMapMatcher queryStringParameterMatcher = null;
	private BodyMatcher bodyMatcher = null;
	private MultiValueMapMatcher headerMatcher = null;
	private HashMapMatcher cookieMatcher = null;
	private BooleanMatcher keepAliveMatcher = null;
	private BodyDTO bodyDTOMatcher = null;
	private BooleanMatcher sslMatcher = null;
	private ObjectMapper objectMapper = ObjectMapperFactory.createObjectMapper();

	public HttpRequestMatcher(HttpRequest httpRequest, MockServerLogger mockServerLogger) {
		this.httpRequest = httpRequest;
		this.mockServerLogger = mockServerLogger;
		if (httpRequest != null) {
			withMethod(httpRequest.getMethod());
			withPath(httpRequest.getPath());
			withQueryStringParameters(httpRequest.getQueryStringParameters());
			withBody(httpRequest.getBody());
			withHeaders(httpRequest.getHeaders());
			withCookies(httpRequest.getCookies());
			withKeepAlive(httpRequest.isKeepAlive());
			withSsl(httpRequest.isSecure());
		}
	}


	public HttpRequestMatcher(Expectation expectation, MockServerLogger mockServerLogger) {
		this.expectation = expectation;
		this.httpRequest = expectation.getHttpRequest();
		this.mockServerLogger = mockServerLogger;
		if (httpRequest != null) {
			withMethod(httpRequest.getMethod());
			withPath(httpRequest.getPath());
			withQueryStringParameters(httpRequest.getQueryStringParameters());
			withBody(httpRequest.getBody());
			withHeaders(httpRequest.getHeaders());
			withCookies(httpRequest.getCookies());
			withKeepAlive(httpRequest.isKeepAlive());
			withSsl(httpRequest.isSecure());
		}
	}

	public Expectation getExpectation() {
		return expectation;
	}

	private void withMethod(NottableString method) {
		this.methodMatcher = new RegexStringMatcher(mockServerLogger, method);
	}

	private void withPath(NottableString path) {
		this.pathMatcher = new RegexStringMatcher(mockServerLogger, path);
	}

	private void withQueryStringParameters(Parameters parameters) {
		this.queryStringParameterMatcher = new MultiValueMapMatcher(mockServerLogger, parameters);
	}

	private void withBody(Body body) {
		if (body != null) {
			switch (body.getType()) {
			case STRING:
				StringBody stringBody = (StringBody) body;
				bodyDTOMatcher = new StringBodyDTO(stringBody);
				if (stringBody.isSubString()) {
					this.bodyMatcher = new SubStringMatcher(mockServerLogger, string(stringBody.getValue(), stringBody.getNot()));
				} else {
					this.bodyMatcher = new ExactStringMatcher(mockServerLogger, string(stringBody.getValue(), stringBody.getNot()));
				}
				break;
			case REGEX:
				RegexBody regexBody = (RegexBody) body;
				bodyDTOMatcher = new RegexBodyDTO(regexBody);
				this.bodyMatcher = new RegexStringMatcher(mockServerLogger, string(regexBody.getValue(), regexBody.getNot()));
				break;
			case PARAMETERS:
				ParameterBody parameterBody = (ParameterBody) body;
				bodyDTOMatcher = new ParameterBodyDTO(parameterBody);
				this.bodyMatcher = new ParameterStringMatcher(mockServerLogger, parameterBody.getValue());
				break;
			case XPATH:
				XPathBody xPathBody = (XPathBody) body;
				bodyDTOMatcher = new XPathBodyDTO(xPathBody);
				this.bodyMatcher = new XPathStringMatcher(mockServerLogger, xPathBody.getValue());
				break;
			case XML:
				XmlBody xmlBody = (XmlBody) body;
				bodyDTOMatcher = new XmlBodyDTO(xmlBody);
				this.bodyMatcher = new XmlStringMatcher(mockServerLogger, xmlBody.getValue());
				break;
			case JSON:
				JsonBody jsonBody = (JsonBody) body;
				bodyDTOMatcher = new JsonBodyDTO(jsonBody);
				this.bodyMatcher = new JsonStringMatcher(mockServerLogger, jsonBody.getValue(), jsonBody.getMatchType());
				break;
			case JSON_SCHEMA:
				JsonSchemaBody jsonSchemaBody = (JsonSchemaBody) body;
				bodyDTOMatcher = new JsonSchemaBodyDTO(jsonSchemaBody);
				this.bodyMatcher = new JsonSchemaMatcher(mockServerLogger, jsonSchemaBody.getValue());
				break;
			case XML_SCHEMA:
				XmlSchemaBody xmlSchemaBody = (XmlSchemaBody) body;
				bodyDTOMatcher = new XmlSchemaBodyDTO(xmlSchemaBody);
				this.bodyMatcher = new XmlSchemaMatcher(mockServerLogger, xmlSchemaBody.getValue());
				break;
			case BINARY:
				BinaryBody binaryBody = (BinaryBody) body;
				bodyDTOMatcher = new BinaryBodyDTO(binaryBody);
				this.bodyMatcher = new BinaryMatcher(mockServerLogger, binaryBody.getValue());
				break;
			}
			if (body.isNot()) {
				this.bodyMatcher = not(this.bodyMatcher);
			}
		}
	}

	private void withHeaders(Headers headers) {
		this.headerMatcher = new MultiValueMapMatcher(mockServerLogger, headers);
	}

	private void withCookies(Cookies cookies) {
		this.cookieMatcher = new HashMapMatcher(mockServerLogger, cookies);
	}

	private void withKeepAlive(Boolean keepAlive) {
		this.keepAliveMatcher = new BooleanMatcher(mockServerLogger, keepAlive);
	}

	private void withSsl(Boolean isSsl) {
		this.sslMatcher = new BooleanMatcher(mockServerLogger, isSsl);
	}

	public HttpRequestMatcherResponse match(final HttpRequest context, HttpRequest request) {
		HttpRequestMatcherResponse httpRequestMatcherResponse = matches(context, request, true);
		return httpRequestMatcherResponse;
	}

	public boolean matches(final HttpRequest context, HttpRequest request) {
		HttpRequestMatcherResponse httpRequestMatcherResponse = matches(context, request, true);
		return httpRequestMatcherResponse.isMatched();
	}

	public boolean matches(HttpRequest request) {
		return matches(null, request, false).isMatched();
	}

	private HttpRequestMatcherResponse matches(HttpRequest context, HttpRequest request, boolean logMatchResults) {

		boolean matches = false;
		HttpRequestMatcherResponse httpRequestMatcherResponse = new HttpRequestMatcherResponse();
		if (isActive()) {
			if (request == this.httpRequest) {
				matches = true;
			} else if (this.httpRequest == null) {
				matches = true;
			} else {
				if (request != null) {
					boolean methodMatches = Strings.isNullOrEmpty(request.getMethod().getValue()) || matches(context, methodMatcher, request.getMethod());
					boolean pathMatches = Strings.isNullOrEmpty(request.getPath().getValue()) || matches(context, pathMatcher, request.getPath()) || matchPath(context, pathMatcher, request.getPath());
					boolean queryStringParametersMatches = matches(context, queryStringParameterMatcher, request.getQueryStringParameters());
					boolean bodyMatches = bodyMatches(context, request);
					boolean headersMatch = matches(context, headerMatcher, request.getHeaders());
					boolean cookiesMatch = matches(context, cookieMatcher, request.getCookies());
					boolean keepAliveMatches = matches(context, keepAliveMatcher, request.isKeepAlive());
					boolean sslMatches = matches(context, sslMatcher, request.isSecure());

					boolean totalResult = methodMatches && pathMatches && queryStringParametersMatches && bodyMatches && headersMatch && cookiesMatch && keepAliveMatches && sslMatches;
					boolean totalResultAfterNotOperatorApplied = request.isNot() == (this.httpRequest.isNot() == (not != totalResult));

					if (logMatchResults) {
						if (!totalResultAfterNotOperatorApplied) {
							StringBuilder becauseBuilder = new StringBuilder();
							becauseBuilder.append("method ").append((methodMatches ? "matched" : "didn't match"));
							becauseBuilder.append(",").append(NEW_LINE).append("path ").append((pathMatches ? "matched" : "didn't match"));
							becauseBuilder.append(",").append(NEW_LINE).append("query ").append((queryStringParametersMatches ? "matched" : "didn't match"));
							becauseBuilder.append(",").append(NEW_LINE).append("body ").append((bodyMatches ? "matched" : "didn't match"));
							becauseBuilder.append(",").append(NEW_LINE).append("headers ").append((headersMatch ? "matched" : "didn't match"));
							becauseBuilder.append(",").append(NEW_LINE).append("cookies ").append((cookiesMatch ? "matched" : "didn't match"));
							becauseBuilder.append(",").append(NEW_LINE).append("keep-alive ").append((keepAliveMatches ? "matched" : "didn't match"));
							becauseBuilder.append(",").append(NEW_LINE).append("ssl ").append((sslMatches ? "matched" : "didn't match"));
							if (request.isNot()) {
								becauseBuilder.append(",").append(NEW_LINE).append("request \'not\' operator is enabled");
							}
							if (this.httpRequest.isNot()) {
								becauseBuilder.append(",").append(NEW_LINE).append("expectation's request \'not\' operator is enabled");
							}
							if (not) {
								becauseBuilder.append(",").append(NEW_LINE).append("expectation's request matcher \'not\' operator is enabled");
							}
							mockServerLogger.info(EXPECTATION_NOT_MATCHED, request, "request:{}" + (totalResult ? "matched " : "didn't match ") + (this.expectation == null ? "request" : "expectation") + ":{}because:{}", request, (this.expectation == null ? this : this.expectation.clone()), becauseBuilder.toString());
							httpRequestMatcherResponse.setBecause(becauseBuilder.toString());
							httpRequestMatcherResponse.setPathMatched(pathMatches);
						} else {
							mockServerLogger.info(EXPECTATION_MATCHED, request, "request:{}matched " + (this.expectation == null ? "request" : "expectation") + ":{}", request, (this.expectation == null ? this : this.expectation.clone()));
						}
					}
					matches = totalResultAfterNotOperatorApplied;
				}
			}
		}
		httpRequestMatcherResponse.setMatched(matches);
		return httpRequestMatcherResponse;
	}

	private boolean bodyMatches(HttpRequest context, HttpRequest request) {
		boolean bodyMatches = true;
		String bodyAsString = request.getBody() != null ? new String(request.getBody().getRawBytes(), request.getBody().getCharset(StandardCharsets.UTF_8)) : "";
		if (!bodyAsString.isEmpty()) {
			if (bodyMatcher instanceof BinaryMatcher) {
				bodyMatches = matches(context, bodyMatcher, request.getBodyAsRawBytes());
			} else {
				if (bodyMatcher instanceof ExactStringMatcher ||
						bodyMatcher instanceof SubStringMatcher ||
						bodyMatcher instanceof RegexStringMatcher ||
						bodyMatcher instanceof XmlStringMatcher) {
					bodyMatches = matches(context, bodyMatcher, string(bodyAsString));
				} else {
					bodyMatches = matches(context, bodyMatcher, bodyAsString);
				}
			}
			if (!bodyMatches) {
				try {
					bodyMatches = bodyDTOMatcher.equals(objectMapper.readValue(bodyAsString, BodyDTO.class));
				} catch (Throwable e) {
					// ignore this exception as this exception would typically get thrown for "normal" HTTP requests (i.e. not clear or retrieve)
				}
			}
		}
		return bodyMatches;
	}

	private <T> boolean matches(HttpRequest context, Matcher<T> matcher, T t) {
		boolean result = false;

		if (matcher == null) {
			result = true;
		} else if (matcher.matches(context, t)) {
			result = true;
		}

		return result;
	}

	private <T> boolean matchPath(HttpRequest context, Matcher<T> matcher, T t) {
		boolean result = false;
		System.out.println(t.toString());
		System.out.println(httpRequest.getPath().toString());

		try {
			org.springframework.web.util.UriTemplate uriTemplate = new org.springframework.web.util.UriTemplate(httpRequest.getPath().toString());
			ObjectMapper mapper = new ObjectMapper();
			Map varMap =  mapper.convertValue(uriTemplate.match(t.toString()), Map.class);
			String URI= uriTemplate.expand(varMap).toString();
//			System.out.println(URI);
//			System.out.println(t.toString());
			if(URI.equals(t.toString()))
				result = Boolean.TRUE;
		}catch(Exception e) {

		}


		//org.springframework.web.util.UriTemplate uriTemplate = new org.springframework.web.util.UriTemplate("/hotels/{hotel}/bookings/{booking}/test?q={test1}");


		return result;
	}

	public boolean isActive() {
		return expectation == null || expectation.isActive();

	}

	public Expectation decrementRemainingMatches() {
		return expectation.decrementRemainingMatches();
	}

	@Override
	public String toString() {
		try {
			return ObjectMapperFactory
					.createObjectMapper()
					.writerWithDefaultPrettyPrinter()
					.writeValueAsString(httpRequest);
		} catch (Exception e) {
			return super.toString();
		}
	}

	@Override
	@JsonIgnore
	public String[] fieldsExcludedFromEqualsAndHashCode() {
		return excludedFields;
	}
}
