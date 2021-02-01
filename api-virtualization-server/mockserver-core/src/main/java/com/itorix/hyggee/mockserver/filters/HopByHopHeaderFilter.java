package com.itorix.hyggee.mockserver.filters;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import com.itorix.hyggee.mockserver.model.Header;
import com.itorix.hyggee.mockserver.model.Headers;
import com.itorix.hyggee.mockserver.model.HttpRequest;

/**
 *   
 */
public class HopByHopHeaderFilter {

    public HttpRequest onRequest(HttpRequest request) {
        if (request != null) {
            List<String> headersToRemove = Arrays.asList(
                "proxy-connection",
                "connection",
                "keep-alive",
                "transfer-encoding",
                "te",
                "trailer",
                "proxy-authorization",
                "proxy-authenticate",
                "upgrade"
            );
            Headers headers = new Headers();
            for (Header header : request.getHeaderList()) {
                if (!headersToRemove.contains(header.getName().getValue().toLowerCase(Locale.ENGLISH))) {
                    headers.withEntry(header);
                }
            }
            HttpRequest clonedRequest = request.clone();
            if (!headers.isEmpty()) {
                clonedRequest.withHeaders(headers);
            }
            return clonedRequest;
        } else {
            return null;
        }
    }
}
