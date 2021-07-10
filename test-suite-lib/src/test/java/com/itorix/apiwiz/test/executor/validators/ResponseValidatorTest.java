package com.itorix.apiwiz.test.executor.validators;

import com.itorix.apiwiz.test.executor.beans.Assertion;
import org.apache.http.message.BasicHeader;
import org.junit.Test;
import org.apache.http.Header;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Test
    public void checkValidHeadersIgnoreCase() throws  Exception {
        Header header = new BasicHeader("cOnTent-type", "json");
        Header[] responseHeaders = new Header[1];
        responseHeaders[0] = header;
        Assertion assertionHeader = getAssertionHeader();

        List<Assertion> expectedHeader = new ArrayList<>();
        expectedHeader.add(assertionHeader);
        Map<String, Integer> testStatus = new HashMap<String, Integer>();

        assertTrue(ResponseValidator.isValidHeaders(responseHeaders, expectedHeader, testStatus));
    }


    @Test(expected = Exception.class)
    public void checkValidHeaders() throws  Exception {
        Header header = new BasicHeader("cOnTent-type", "json");
        Header[] responseHeaders = new Header[1];
        responseHeaders[0] = header;
        Assertion assertionHeader = getAssertionHeader();
        assertionHeader.setIgnoreCase(false);

        List<Assertion> expectedHeader = new ArrayList<>();
        expectedHeader.add(assertionHeader);
        Map<String, Integer> testStatus = new HashMap<String, Integer>();

        ResponseValidator.isValidHeaders(responseHeaders, expectedHeader, testStatus);
    }

    private Assertion getAssertionHeader() {
        Assertion assertionHeader = new Assertion();
        assertionHeader.setName("Content-Type");
        assertionHeader.setIgnoreCase(true);
        assertionHeader.setValue("json");
        assertionHeader.setCondition("equalTO");
        return assertionHeader;
    }
}