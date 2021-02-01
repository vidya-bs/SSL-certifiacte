package com.itorix.hyggee.mockserver.verify;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.itorix.hyggee.mockserver.model.HttpRequest;
import com.itorix.hyggee.mockserver.model.ObjectWithJsonToString;

/**
 *   
 */
public class VerificationSequence extends ObjectWithJsonToString {
    private List<HttpRequest> httpRequests = new ArrayList<HttpRequest>();

    public static VerificationSequence verificationSequence() {
        return new VerificationSequence();
    }

    public VerificationSequence withRequests(HttpRequest... httpRequests) {
        Collections.addAll(this.httpRequests, httpRequests);
        return this;
    }

    public VerificationSequence withRequests(List<HttpRequest> httpRequests) {
        this.httpRequests = httpRequests;
        return this;
    }

    public List<HttpRequest> getHttpRequests() {
        return httpRequests;
    }
}
