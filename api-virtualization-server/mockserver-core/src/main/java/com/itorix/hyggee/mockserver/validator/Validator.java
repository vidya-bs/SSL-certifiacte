package com.itorix.hyggee.mockserver.validator;

/**
 *   
 */
public interface Validator<T> {

    public String isValid(T t);
}
