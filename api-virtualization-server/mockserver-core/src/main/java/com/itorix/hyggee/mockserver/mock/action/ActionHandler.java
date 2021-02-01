package com.itorix.hyggee.mockserver.mock.action;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.SettableFuture;
import com.itorix.hyggee.mockserver.client.netty.NettyHttpClient;
import com.itorix.hyggee.mockserver.client.netty.SocketCommunicationException;
import com.itorix.hyggee.mockserver.client.netty.SocketConnectionException;
import com.itorix.hyggee.mockserver.client.netty.proxy.ProxyConfiguration;
import com.itorix.hyggee.mockserver.client.serialization.ObjectMapperFactory;
import com.itorix.hyggee.mockserver.client.serialization.curl.HttpRequestToCurlSerializer;
import com.itorix.hyggee.mockserver.configuration.ConfigurationProperties;
import com.itorix.hyggee.mockserver.filters.HopByHopHeaderFilter;
import com.itorix.hyggee.mockserver.log.model.ExpectationMatchLogEntry;
import com.itorix.hyggee.mockserver.log.model.RequestLogEntry;
import com.itorix.hyggee.mockserver.log.model.RequestResponseLogEntry;
import com.itorix.hyggee.mockserver.logging.MockServerLogger;
import com.itorix.hyggee.mockserver.mock.Expectation;
import com.itorix.hyggee.mockserver.mock.HttpStateHandler;
import com.itorix.hyggee.mockserver.mock.MockServerMatcherResponse;
import com.itorix.hyggee.mockserver.model.*;
import com.itorix.hyggee.mockserver.responsewriter.ResponseWriter;
import com.itorix.hyggee.mockserver.scheduler.Scheduler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.itorix.hyggee.mockserver.configuration.ConfigurationProperties.enableCORSForAPI;
import static com.itorix.hyggee.mockserver.configuration.ConfigurationProperties.enableCORSForAllResponses;
import static com.itorix.hyggee.mockserver.cors.CORSHeaders.isPreflightRequest;
import static com.itorix.hyggee.mockserver.log.model.MessageLogEntry.LogMessageType.*;
import static com.itorix.hyggee.mockserver.model.HttpResponse.notFoundResponse;
import static io.netty.handler.codec.http.HttpHeaderNames.HOST;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;

/**
 *   
 */
@Component("actionHandler")
public class ActionHandler {

    public static final AttributeKey<InetSocketAddress> REMOTE_SOCKET = AttributeKey.valueOf("REMOTE_SOCKET");

    private final HttpStateHandler httpStateHandler;
    private final Scheduler scheduler;
    private MockServerLogger mockServerLogger;
    private HttpResponseActionHandler httpResponseActionHandler;
    private HttpResponseTemplateActionHandler httpResponseTemplateActionHandler;
    private HttpResponseClassCallbackActionHandler httpResponseClassCallbackActionHandler;
    private HttpResponseObjectCallbackActionHandler httpResponseObjectCallbackActionHandler;
    private HttpForwardActionHandler httpForwardActionHandler;
    private HttpForwardTemplateActionHandler httpForwardTemplateActionHandler;
    private HttpForwardClassCallbackActionHandler httpForwardClassCallbackActionHandler;
    private HttpForwardObjectCallbackActionHandler httpForwardObjectCallbackActionHandler;
    private HttpOverrideForwardedRequestActionHandler httpOverrideForwardedRequestCallbackActionHandler;
    private HttpErrorActionHandler httpErrorActionHandler;

    // forwarding
    private NettyHttpClient httpClient;
    private HopByHopHeaderFilter hopByHopHeaderFilter = new HopByHopHeaderFilter();
    private HttpRequestToCurlSerializer httpRequestToCurlSerializer = new HttpRequestToCurlSerializer();
    private HttpResponse responseBody;
    
    
    public ActionHandler(HttpStateHandler httpStateHandler, ProxyConfiguration proxyConfiguration) {
        this.httpStateHandler = httpStateHandler;
        this.scheduler = httpStateHandler.getScheduler();
        this.mockServerLogger = httpStateHandler.getMockServerLogger();
        this.httpClient = new NettyHttpClient(proxyConfiguration);
        this.httpResponseActionHandler = new HttpResponseActionHandler();
        this.httpResponseTemplateActionHandler = new HttpResponseTemplateActionHandler(mockServerLogger);
        this.httpResponseClassCallbackActionHandler = new HttpResponseClassCallbackActionHandler(mockServerLogger);
        this.httpResponseObjectCallbackActionHandler = new HttpResponseObjectCallbackActionHandler(httpStateHandler);
        this.httpForwardActionHandler = new HttpForwardActionHandler(mockServerLogger, httpClient);
        this.httpForwardTemplateActionHandler = new HttpForwardTemplateActionHandler(mockServerLogger, httpClient);
        this.httpForwardClassCallbackActionHandler = new HttpForwardClassCallbackActionHandler(mockServerLogger, httpClient);
        this.httpForwardObjectCallbackActionHandler = new HttpForwardObjectCallbackActionHandler(httpStateHandler, httpClient);
        this.httpOverrideForwardedRequestCallbackActionHandler = new HttpOverrideForwardedRequestActionHandler(mockServerLogger, httpClient);
        this.httpErrorActionHandler = new HttpErrorActionHandler();
    }
    public Map<?,?> processRequest(final HttpRequest request, 
   		 HttpResponse finalResponse, 
   		final ResponseWriter responseWriter, 
   		final ChannelHandlerContext ctx, 
   		Set<String> localAddresses, 
   		boolean proxyThisRequest, 
   		final boolean synchronous,
   		boolean isSuccess) {
    		return processAction1(request, finalResponse, responseWriter, ctx, localAddresses, proxyThisRequest, synchronous,isSuccess);
   		}
    
    private void logResponse(HttpResponse response){ 
    	responseBody = response;
//    	String content = response.getBodyAsString();
//    	return content;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
	private Map<?,?> processAction1(final HttpRequest request, 
    		 HttpResponse finalResponse, 
    		final ResponseWriter responseWriter, 
    		final ChannelHandlerContext ctx, 
    		Set<String> localAddresses, 
    		boolean proxyThisRequest, 
    		final boolean synchronous,
    		boolean isSuccess) {
    	Map responseMap = new HashMap();
    	ActionHandleResponse mockServerMatcherResponse =httpStateHandler.firstMatchingExpectation(request);
        final Expectation expectation = mockServerMatcherResponse.getExpectation();
        if (request.getHeaders().containsEntry("x-forwarded-by", "MockServer")) {
            mockServerLogger.trace("Received \"x-forwarded-by\" header caused by exploratory HTTP proxy - falling back to no proxy: {}", request);
            returnNotFound(responseWriter, request);
        } else if (expectation != null && expectation.getAction() != null) {
        	responseMap.put("expectationId", expectation.getId());
            final Action action = expectation.getAction();
            isSuccess=true;
            switch (action.getType()) {
                case RESPONSE: {
                    final HttpResponse httpResponse = (HttpResponse) action;
                    finalResponse =  (HttpResponse) action;
                    httpStateHandler.log(new ExpectationMatchLogEntry(request, expectation));
                    scheduler.schedule(new Runnable() {
                        public void run() {
                            HttpResponse response = httpResponseActionHandler.handle(httpResponse);
                            responseWriter.writeResponse(request, response, false);
                            mockServerLogger.info(EXPECTATION_RESPONSE, request, "returning response:{}for request:{}for action:{}", response, request, action);
                        }
                    }, httpResponse.getDelay(), synchronous);
                    break;
                }
                case RESPONSE_TEMPLATE: {
                    final HttpTemplate httpTemplate = (HttpTemplate) action;
                    httpStateHandler.log(new ExpectationMatchLogEntry(request, expectation));
                    scheduler.schedule(new Runnable() {
                        public void run() {
                            HttpResponse response = httpResponseTemplateActionHandler.handle(expectation, httpTemplate, request);
                            logResponse(response);
                            responseWriter.writeResponse(request, response, false);
                            mockServerLogger.info(EXPECTATION_RESPONSE, request, "returning response:{}for request:{}for action:{}", response, request, action);
                        }
                    }, httpTemplate.getDelay(), synchronous);
                    finalResponse = this.responseBody;
                    this.responseBody = null;
                    break;
                }
                case RESPONSE_CLASS_CALLBACK: {
                    httpStateHandler.log(new ExpectationMatchLogEntry(request, expectation));
                    final HttpClassCallback classCallback = (HttpClassCallback) action;
                    scheduler.submit(new Runnable() {
                        public void run() {
                            HttpResponse response = httpResponseClassCallbackActionHandler.handle(classCallback, request);
                            responseWriter.writeResponse(request, response, false);
                            mockServerLogger.info(EXPECTATION_RESPONSE, request, "returning response:{}for request:{}for action:{}", response, request, action);
                        }
                    }, synchronous);
                    break;
                }
                case RESPONSE_OBJECT_CALLBACK: {
                    httpStateHandler.log(new ExpectationMatchLogEntry(request, expectation));
                    final HttpObjectCallback objectCallback = (HttpObjectCallback) action;
                    scheduler.submit(new Runnable() {
                        public void run() {
                            httpResponseObjectCallbackActionHandler.handle(objectCallback, request, responseWriter);
                        }
                    }, synchronous);
                    break;
                }
                case FORWARD: {
                    final HttpForward httpForward = (HttpForward) action;
                    scheduler.schedule(new Runnable() {
                        public void run() {
                            final SettableFuture<HttpResponse> responseFuture = httpForwardActionHandler.handle(httpForward, request);
                            scheduler.submit(responseFuture, new Runnable() {
                                public void run() {
                                    try {
                                        HttpResponse response = responseFuture.get();
                                        responseWriter.writeResponse(request, response, false);
                                        httpStateHandler.log(new RequestResponseLogEntry(request, response));
                                        mockServerLogger.info(EXPECTATION_RESPONSE, request, "returning response:{}for request:{}for action:{}", response, request, action);
                                    } catch (Exception ex) {
                                        mockServerLogger.error(request, ex, ex.getMessage());
                                    }
                                }
                            }, synchronous);
                        }
                    }, httpForward.getDelay(), synchronous);
                    break;
                }
                case FORWARD_TEMPLATE: {
                    final HttpTemplate httpTemplate = (HttpTemplate) action;
                    scheduler.schedule(new Runnable() {
                        public void run() {
                            final SettableFuture<HttpResponse> responseFuture = httpForwardTemplateActionHandler.handle(expectation,httpTemplate, request);
                            scheduler.submit(responseFuture, new Runnable() {
                                public void run() {
                                    try {
                                        HttpResponse response = responseFuture.get();
                                        responseWriter.writeResponse(request, response, false);
                                        httpStateHandler.log(new RequestResponseLogEntry(request, response));
                                        mockServerLogger.info(EXPECTATION_RESPONSE, request, "returning response:{}for request:{}for action:{}", response, request, action);
                                    } catch (Exception ex) {
                                        mockServerLogger.error(request, ex, ex.getMessage());
                                    }
                                }
                            }, synchronous);
                        }
                    }, httpTemplate.getDelay(), synchronous);
                    break;
                }
                case FORWARD_CLASS_CALLBACK: {
                    httpStateHandler.log(new ExpectationMatchLogEntry(request, expectation));
                    final HttpClassCallback classCallback = (HttpClassCallback) action;
                    scheduler.submit(new Runnable() {
                        public void run() {
                            final SettableFuture<HttpResponse> responseFuture = httpForwardClassCallbackActionHandler.handle(classCallback, request);
                            scheduler.submit(responseFuture, new Runnable() {
                                public void run() {
                                    try {
                                        HttpResponse response = responseFuture.get();
                                        responseWriter.writeResponse(request, response, false);
                                        mockServerLogger.info(EXPECTATION_RESPONSE, request, "returning response:{}for request:{}for action:{}", response, request, action);
                                    } catch (Exception ex) {
                                        mockServerLogger.error(request, ex, ex.getMessage());
                                    }
                                }
                            }, synchronous);
                        }
                    }, synchronous);
                    break;
                }
                case FORWARD_OBJECT_CALLBACK: {
                    httpStateHandler.log(new ExpectationMatchLogEntry(request, expectation));
                    final HttpObjectCallback objectCallback = (HttpObjectCallback) action;
                    scheduler.submit(new Runnable() {
                        public void run() {
                            httpForwardObjectCallbackActionHandler.handle(objectCallback, request, responseWriter, synchronous);
                        }
                    }, synchronous);
                    break;
                }
                case FORWARD_REPLACE: {
                    httpStateHandler.log(new ExpectationMatchLogEntry(request, expectation));
                    final HttpOverrideForwardedRequest httpOverrideForwardedRequest = (HttpOverrideForwardedRequest) action;
                    scheduler.schedule(new Runnable() {
                        public void run() {
                            final SettableFuture<HttpResponse> responseFuture = httpOverrideForwardedRequestCallbackActionHandler.handle(httpOverrideForwardedRequest, request);
                            scheduler.submit(responseFuture, new Runnable() {
                                public void run() {
                                    try {
                                        HttpResponse response = responseFuture.get();
                                        responseWriter.writeResponse(request, response, false);
                                        mockServerLogger.info(EXPECTATION_RESPONSE, request, "returning response:{}for request:{}for action:{}", response, request, action);
                                    } catch (Exception ex) {
                                        mockServerLogger.error(request, ex, ex.getMessage());
                                    }
                                }
                            }, synchronous);
                        }
                    }, httpOverrideForwardedRequest.getDelay(), synchronous);
                    break;
                }
                case ERROR: {
                    final HttpError httpError = (HttpError) action;
                    httpStateHandler.log(new ExpectationMatchLogEntry(request, expectation));
                    scheduler.schedule(new Runnable() {
                        public void run() {
                            httpErrorActionHandler.handle(httpError, ctx);
                            mockServerLogger.info(EXPECTATION_RESPONSE, request, "returning error:{}for request:{}for action:{}", httpError, request, action);
                        }
                    }, httpError.getDelay(), synchronous);
                    break;
                }
            }
        } else if ((enableCORSForAPI() || enableCORSForAllResponses()) && isPreflightRequest(request)) {

            responseWriter.writeResponse(request, OK);

        } 
        else {
        	mockServerLogger.info(EXPECTATION_NOT_MATCHED, "notFoundBody" , "inside else Condition ");
        	ObjectMapper objectMapper = ObjectMapperFactory.createObjectMapper();
        	
        	String notFoundBody= "";
        	try {
        		if(mockServerMatcherResponse.getMatchers() != null) {
        			notFoundBody = objectMapper.writeValueAsString(mockServerMatcherResponse.getMatchers());
        			responseMap.put("expectationId",mockServerMatcherResponse.getMatchers().get(0).getExpectationId());
        			responseMap.put("groupName",mockServerMatcherResponse.getMatchers().get(0).getGroupName());
        		}
        		else {
        			notFoundBody = " {\"message\" : \"no Expectations available\"}";
        			responseMap.put("expectationId",null);
        			responseMap.put("groupName",null);
        		}
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
        	mockServerLogger.debug(EXPECTATION_NOT_MATCHED, "notFoundBody" , notFoundBody);
        	HttpResponse response = notFoundResponse(notFoundBody);
        	Header header  = new Header("content-type", "applaction/json");
        	response.withHeader(header);
            if (request.getHeaders().containsEntry("x-forwarded-by", "MockServer")) {
                response.withHeader("x-forwarded-by", "MockServer");
            } else {
                httpStateHandler.log(new RequestLogEntry(request));
                mockServerLogger.info(EXPECTATION_NOT_MATCHED, request, "2.... no expectation for:{}returning response:{}", request, notFoundResponse());
            }
            responseWriter.writeResponse(request, response, false);
            
            finalResponse = response;
            try {
				mockServerLogger.info(EXPECTATION_NOT_MATCHED, "notFoundResponse" , objectMapper.writeValueAsString(response));
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
        	//finalResponse = returnNotFound(responseWriter, request);
        }
        
        responseMap.put("response", finalResponse);
        responseMap.put("success", isSuccess);
        
        return responseMap;
    }

    private HttpResponse returnNotFound(ResponseWriter responseWriter, HttpRequest request) {
        HttpResponse response = notFoundResponse();
        if (request.getHeaders().containsEntry("x-forwarded-by", "MockServer")) {
            response.withHeader("x-forwarded-by", "MockServer");
        } else {
            httpStateHandler.log(new RequestLogEntry(request));
            mockServerLogger.info(EXPECTATION_NOT_MATCHED, request, "3.... no expectation for:{}returning response:{}", request, notFoundResponse());
        }
        responseWriter.writeResponse(request, response, false);
        return response;
    }

}
