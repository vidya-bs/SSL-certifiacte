package com.itorix.mockserver.common.model;

import javax.servlet.http.Cookie;

import org.springframework.util.MultiValueMap;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MockRequest {

    String path;
    String method;
    MultiValueMap<String, String> headers;
    MultiValueMap<String, String> requestParams;
    MultiValueMap<String, String> formParams;
    Cookie[] cookie;
    String body;
}
