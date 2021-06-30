package com.itorix.apiwiz.test.executor.validators;

import org.junit.Test;

import static org.junit.Assert.*;

public class ResponseValidatorTest {

    @Test
    public void checkAssertionForInteger() throws Exception {
            ResponseValidator.checkAssertion(200, "200", "equalTO", false);
            assertTrue(true);
    }

    @Test
    public void checkAssertionForLong() throws Exception {
        ResponseValidator.checkAssertion(200L, "200", "equalTO", false);
        assertTrue(true);
    }

    @Test
    public void checkAssertionForFloat() throws Exception {
        ResponseValidator.checkAssertion(0.01, "0.01", "equalTO", false);
        assertTrue(true);
    }

    @Test
    public void checkAssertionForString() throws Exception {
        ResponseValidator.checkAssertion("200", "200", "equalTO", false);
        assertTrue(true);
    }

    @Test
    public void checkAssertionForBoolean() throws Exception {
        ResponseValidator.checkAssertion(true, "true", "equalTO", false);
        assertTrue(true);
    }
}