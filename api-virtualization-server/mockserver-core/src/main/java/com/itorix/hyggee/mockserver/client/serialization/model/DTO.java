package com.itorix.hyggee.mockserver.client.serialization.model;


public interface DTO<T> {

    T buildObject();
}
