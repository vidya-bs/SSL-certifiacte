package com.itorix.mockserver.common.model;

import org.springframework.util.MultiValueMap;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MockResponse {
    MultiValueMap<String, String> headers;
    String body;
    int statusCode;
    String statusMessage;
}
