package com.itorix.hyggee.mockserver.client.serialization.model;

import com.itorix.hyggee.mockserver.model.HttpRequest;
import com.itorix.hyggee.mockserver.model.ObjectWithReflectiveEqualsHashCodeToString;
import com.itorix.hyggee.mockserver.verify.VerificationSequence;

import java.util.ArrayList;
import java.util.List;

/**
 *   
 */
public class VerificationSequenceDTO extends ObjectWithReflectiveEqualsHashCodeToString implements DTO<VerificationSequence> {
    private List<HttpRequestDTO> httpRequests = new ArrayList<HttpRequestDTO>();

    public VerificationSequenceDTO(VerificationSequence verification) {
        if (verification != null) {
            for (HttpRequest httpRequest : verification.getHttpRequests()) {
                httpRequests.add(new HttpRequestDTO(httpRequest));
            }
        }
    }

    public VerificationSequenceDTO() {
    }

    public VerificationSequence buildObject() {
        List<HttpRequest> httpRequests = new ArrayList<HttpRequest>();
        for (HttpRequestDTO httpRequest : this.httpRequests) {
            httpRequests.add(httpRequest.buildObject());
        }
        return new VerificationSequence()
            .withRequests(httpRequests);
    }

    public List<HttpRequestDTO> getHttpRequests() {
        return httpRequests;
    }

    public VerificationSequenceDTO setHttpRequests(List<HttpRequestDTO> httpRequests) {
        this.httpRequests = httpRequests;
        return this;
    }
}
