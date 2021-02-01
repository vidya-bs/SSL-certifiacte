package com.itorix.hyggee.mockserver.mock;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.itorix.hyggee.mockserver.matchers.TimeToLive;
import com.itorix.hyggee.mockserver.matchers.Times;
import com.itorix.hyggee.mockserver.model.*;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Component;

/**
 *   
 */

public class Expectation extends ObjectWithJsonToString {
	
	private String id;
    private final HttpRequest httpRequest;
    private final Times times;
    private final TimeToLive timeToLive;
    private HttpResponse httpResponse;
    private HttpTemplate httpResponseTemplate;
    private HttpClassCallback httpResponseClassCallback;
    private HttpObjectCallback httpResponseObjectCallback;
    private HttpForward httpForward;
    private HttpTemplate httpForwardTemplate;
    private HttpClassCallback httpForwardClassCallback;
    private HttpObjectCallback httpForwardObjectCallback;
    private HttpOverrideForwardedRequest httpOverrideForwardedRequest;
    private HttpError httpError;

    public Expectation(HttpRequest httpRequest) {
        this(httpRequest, Times.unlimited(), TimeToLive.unlimited());
    }
    public Expectation() {
        this(null, Times.unlimited(), TimeToLive.unlimited());
    }
    public Expectation(HttpRequest httpRequest, Times times, TimeToLive timeToLive) {
        this.httpRequest = httpRequest;
        this.times = times;
        this.timeToLive = timeToLive;
    }

    public HttpRequest getHttpRequest() {
        return httpRequest;
    }

    public HttpResponse getHttpResponse() {
        return httpResponse;
    }

    public HttpTemplate getHttpResponseTemplate() {
        return httpResponseTemplate;
    }

    public HttpClassCallback getHttpResponseClassCallback() {
        return httpResponseClassCallback;
    }

    public HttpObjectCallback getHttpResponseObjectCallback() {
        return httpResponseObjectCallback;
    }

    public HttpForward getHttpForward() {
        return httpForward;
    }

    public HttpTemplate getHttpForwardTemplate() {
        return httpForwardTemplate;
    }

    public HttpClassCallback getHttpForwardClassCallback() {
        return httpForwardClassCallback;
    }

    public HttpObjectCallback getHttpForwardObjectCallback() {
        return httpForwardObjectCallback;
    }

    public HttpOverrideForwardedRequest getHttpOverrideForwardedRequest() {
        return httpOverrideForwardedRequest;
    }

    public HttpError getHttpError() {
        return httpError;
    }

    @JsonIgnore
    public Action getAction() {
        if (httpResponse != null) {
            return getHttpResponse();
        } else if (httpResponseTemplate != null) {
            return getHttpResponseTemplate();
        } else if (httpResponseClassCallback != null) {
            return getHttpResponseClassCallback();
        } else if (httpResponseObjectCallback != null) {
            return getHttpResponseObjectCallback();
        } else if (httpForward != null) {
            return getHttpForward();
        } else if (httpForwardTemplate != null) {
            return getHttpForwardTemplate();
        } else if (httpForwardClassCallback != null) {
            return getHttpForwardClassCallback();
        } else if (httpForwardObjectCallback != null) {
            return getHttpForwardObjectCallback();
        } else if (httpOverrideForwardedRequest != null) {
            return getHttpOverrideForwardedRequest();
        } else if (httpError != null) {
            return getHttpError();
        } else {
            return null;
        }
    }

    public Times getTimes() {
        return times;
    }

    public TimeToLive getTimeToLive() {
        return timeToLive;
    }

    public Expectation thenRespond(HttpResponse httpResponse) {
        if (httpResponse != null) {
            validationErrors("a response", httpResponse.getType());
            this.httpResponse = httpResponse;
        }
        return this;
    }

    public Expectation thenRespond(HttpTemplate httpTemplate) {
        if (httpTemplate != null) {
            httpTemplate.setActionType(Action.Type.RESPONSE_TEMPLATE);
            validationErrors("a response template", httpTemplate.getType());
            this.httpResponseTemplate = httpTemplate;
        }
        return this;
    }

    public Expectation thenRespond(HttpClassCallback httpClassCallback) {
        if (httpClassCallback != null) {
            httpClassCallback.withActionType(Action.Type.RESPONSE_CLASS_CALLBACK);
            validationErrors("a response class callback", httpClassCallback.getType());
            this.httpResponseClassCallback = httpClassCallback;
        }
        return this;
    }

    public Expectation thenRespond(HttpObjectCallback httpObjectCallback) {
        if (httpObjectCallback != null) {
            httpObjectCallback.withActionType(Action.Type.RESPONSE_OBJECT_CALLBACK);
            validationErrors("a response object callback", httpObjectCallback.getType());
            this.httpResponseObjectCallback = httpObjectCallback;
        }
        return this;
    }

    public Expectation thenForward(HttpForward httpForward) {
        if (httpForward != null) {
            validationErrors("a forward", httpForward.getType());
            this.httpForward = httpForward;
        }
        return this;
    }

    public Expectation thenForward(HttpTemplate httpTemplate) {
        if (httpTemplate != null) {
            httpTemplate.setActionType(Action.Type.FORWARD_TEMPLATE);
            validationErrors("a forward template", httpTemplate.getType());
            this.httpForwardTemplate = httpTemplate;
        }
        return this;
    }

    public Expectation thenForward(HttpClassCallback httpClassCallback) {
        if (httpClassCallback != null) {
            httpClassCallback.withActionType(Action.Type.FORWARD_CLASS_CALLBACK);
            validationErrors("a forward class callback", httpClassCallback.getType());
            this.httpForwardClassCallback = httpClassCallback;
        }
        return this;
    }

    public Expectation thenForward(HttpObjectCallback httpObjectCallback) {
        if (httpObjectCallback != null) {
            httpObjectCallback.withActionType(Action.Type.FORWARD_OBJECT_CALLBACK);
            validationErrors("a forward object callback", httpObjectCallback.getType());
            this.httpForwardObjectCallback = httpObjectCallback;
        }
        return this;
    }

    public Expectation thenForward(HttpOverrideForwardedRequest httpOverrideForwardedRequest) {
        if (httpOverrideForwardedRequest != null) {
            validationErrors("a forward replace", httpOverrideForwardedRequest.getType());
            this.httpOverrideForwardedRequest = httpOverrideForwardedRequest;
        }
        return this;
    }

    public Expectation thenError(HttpError httpError) {
        if (httpError != null) {
            validationErrors("an error", httpError.getType());
            this.httpError = httpError;
        }
        return this;
    }

    private void validationErrors(String actionDescription, Action.Type actionType) {
        if (actionType != Action.Type.RESPONSE && httpResponse != null) {
            throw new IllegalArgumentException("It is not possible to set " + actionDescription + " once a response has been set");
        }
        if (actionType != Action.Type.RESPONSE_TEMPLATE && httpResponseTemplate != null) {
            throw new IllegalArgumentException("It is not possible to set " + actionDescription + " once a response template has been set");
        }
        if (actionType != Action.Type.RESPONSE_CLASS_CALLBACK && httpResponseClassCallback != null) {
            throw new IllegalArgumentException("It is not possible to set " + actionDescription + " once a class callback has been set");
        }
        if (actionType != Action.Type.RESPONSE_OBJECT_CALLBACK && httpResponseObjectCallback != null) {
            throw new IllegalArgumentException("It is not possible to set " + actionDescription + " once an object callback has been set");
        }
        if (actionType != Action.Type.FORWARD && httpForward != null) {
            throw new IllegalArgumentException("It is not possible to set " + actionDescription + " once a forward has been set");
        }
        if (actionType != Action.Type.FORWARD_TEMPLATE && httpForwardTemplate != null) {
            throw new IllegalArgumentException("It is not possible to set " + actionDescription + " once a forward template has been set");
        }
        if (actionType != Action.Type.FORWARD_CLASS_CALLBACK && httpForwardClassCallback != null) {
            throw new IllegalArgumentException("It is not possible to set " + actionDescription + " once a class callback has been set");
        }
        if (actionType != Action.Type.FORWARD_OBJECT_CALLBACK && httpForwardObjectCallback != null) {
            throw new IllegalArgumentException("It is not possible to set " + actionDescription + " once an object callback has been set");
        }
        if (actionType != Action.Type.FORWARD_REPLACE && httpOverrideForwardedRequest != null) {
            throw new IllegalArgumentException("It is not possible to set " + actionDescription + " once a forward replace has been set");
        }
        if (actionType != Action.Type.ERROR && httpError != null) {
            throw new IllegalArgumentException("It is not possible to set " + actionDescription + " callback once an error has been set");
        }
    }

    @JsonIgnore
    public boolean isActive() {
        return hasRemainingMatches() && isStillAlive();
    }

    private boolean hasRemainingMatches() {
        return times == null || times.greaterThenZero();
    }

    private boolean isStillAlive() {
        return timeToLive == null || timeToLive.stillAlive();
    }

    public Expectation decrementRemainingMatches() {
        if (times != null) {
            times.decrement();
        }
        return this;
    }

    public boolean contains(HttpRequest httpRequest) {
        return httpRequest != null && this.httpRequest.equals(httpRequest);
    }

    public Expectation clone() {
        return new Expectation(httpRequest, times.clone(), timeToLive)
            .thenRespond(httpResponse)
            .thenRespond(httpResponseTemplate)
            .thenRespond(httpResponseClassCallback)
            .thenRespond(httpResponseObjectCallback)
            .thenForward(httpForward)
            .thenForward(httpForwardTemplate)
            .thenForward(httpForwardClassCallback)
            .thenForward(httpForwardObjectCallback)
            .thenForward(httpOverrideForwardedRequest)
            .thenError(httpError);
    }
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
}
