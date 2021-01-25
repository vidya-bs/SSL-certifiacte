package com.itorix.hyggee.mockserver.client.serialization.model;

import com.itorix.hyggee.mockserver.model.ObjectWithReflectiveEqualsHashCodeToString;
import com.itorix.hyggee.mockserver.verify.Verification;

import static com.itorix.hyggee.mockserver.model.HttpRequest.request;
import static com.itorix.hyggee.mockserver.verify.Verification.verification;
import static com.itorix.hyggee.mockserver.verify.VerificationTimes.once;

/**
 *   
 */
public class VerificationDTO extends ObjectWithReflectiveEqualsHashCodeToString implements DTO<Verification> {
    private HttpRequestDTO httpRequest;
    private VerificationTimesDTO times;

    public VerificationDTO(Verification verification) {
        if (verification != null) {
            httpRequest = new HttpRequestDTO(verification.getHttpRequest());
            times = new VerificationTimesDTO(verification.getTimes());
        }
    }

    public VerificationDTO() {
    }

    public Verification buildObject() {
        return verification()
            .withRequest((httpRequest != null ? httpRequest.buildObject() : request()))
            .withTimes((times != null ? times.buildObject() : once()));
    }

    public HttpRequestDTO getHttpRequest() {
        return httpRequest;
    }

    public VerificationDTO setHttpRequest(HttpRequestDTO httpRequest) {
        this.httpRequest = httpRequest;
        return this;
    }

    public VerificationTimesDTO getTimes() {
        return times;
    }

    public VerificationDTO setTimes(VerificationTimesDTO times) {
        this.times = times;
        return this;
    }
}
