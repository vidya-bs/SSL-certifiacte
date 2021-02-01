package com.itorix.hyggee.mockserver.verify;

import static com.itorix.hyggee.mockserver.model.HttpRequest.request;

import com.itorix.hyggee.mockserver.model.HttpRequest;
import com.itorix.hyggee.mockserver.model.ObjectWithJsonToString;

/**
 *   
 */
public class Verification extends ObjectWithJsonToString {
    private HttpRequest httpRequest = request();
    private VerificationTimes times = VerificationTimes.atLeast(1);

    public static Verification verification() {
        return new Verification();
    }

    public Verification withRequest(HttpRequest httpRequest) {
        this.httpRequest = httpRequest;
        return this;
    }

    public HttpRequest getHttpRequest() {
        return httpRequest;
    }

    public Verification withTimes(VerificationTimes times) {
        this.times = times;
        return this;
    }

    public VerificationTimes getTimes() {
        return times;
    }
}
