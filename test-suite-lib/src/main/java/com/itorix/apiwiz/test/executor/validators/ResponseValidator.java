package com.itorix.apiwiz.test.executor.validators;

import com.itorix.apiwiz.test.exception.HaltExecution;
import com.itorix.apiwiz.test.executor.beans.Assertion;
import com.itorix.apiwiz.test.executor.beans.Response;
import com.itorix.apiwiz.test.executor.beans.ResponseAssertions;
import com.itorix.apiwiz.test.executor.beans.ResponseBodyValidation;
import net.minidev.json.JSONArray;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.testng.Assert;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ResponseValidator {

    public static boolean isValidHeaders(Header[] headers, List<Assertion> assertionHeaders,
            Map<String, Integer> testStatus) throws Exception {
        if (assertionHeaders != null && !assertionHeaders.isEmpty()) {
            if (headers == null) {
                Assert.fail("No Response or Headers");
                return false;
            }
            Map<String, String> responseHeaders = Arrays.stream(headers)
                    .collect(Collectors.toMap(Header::getName, Header::getValue));
            for (Assertion responseHeaderAssertion : assertionHeaders) {
                try {
                    if (responseHeaderAssertion.isIgnoreCase()) {
                        // converting response headers to lowerCase
                        Map<String, String> lowerCaseHeader = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
                        lowerCaseHeader.putAll(responseHeaders);
                        checkAssertion(lowerCaseHeader.get(responseHeaderAssertion.getName().toLowerCase()),
                                responseHeaderAssertion.getValue(), responseHeaderAssertion.getCondition(),
                                responseHeaderAssertion.isContinueOnError());
                    } else {
                        checkAssertion(responseHeaders.get(responseHeaderAssertion.getName()),
                                responseHeaderAssertion.getValue(), responseHeaderAssertion.getCondition(),
                                responseHeaderAssertion.isContinueOnError());
                    }
                    responseHeaderAssertion.setStatus("PASS");
                } catch (Exception e) {
                    responseHeaderAssertion.setStatus("FAIL");
                    if (testStatus.get("FAIL") == null) {
                        testStatus.put("FAIL", 1);
                    } else {
                        testStatus.put("FAIL", testStatus.get("FAIL") + 1);
                    }
                    responseHeaderAssertion.setMessage(e.getMessage());
                    if (!responseHeaderAssertion.isContinueOnError()) {
                        throw new HaltExecution("Assertion Failed, Aborting...", e);
                    }
                }
            }
        }
        return true;
    }

    public static boolean validateStatusAndMessage(HttpResponse actualResponse, Response expectedResponse,
            Map<String, Integer> testStatus) throws Exception {
        if (actualResponse == null) {
            Assert.fail("Invalid Response for testCase");
            if (testStatus.get("FAIL") == null) {
                testStatus.put("FAIL", 1);
            } else {
                testStatus.put("FAIL", testStatus.get("FAIL") + 1);
            }
            return false;
        }

        if (expectedResponse != null && expectedResponse.getAssertions() != null
                && expectedResponse.getAssertions().getStatus() != null) {
            for (Assertion responeStatusAssertion : expectedResponse.getAssertions().getStatus()) {
                if (responeStatusAssertion.getName() != null) {
                    try {
                        if (responeStatusAssertion.getName().toLowerCase().equals("code")) {
                            checkAssertion(String.valueOf(actualResponse.getStatusLine().getStatusCode()),
                                    responeStatusAssertion.getValue(), responeStatusAssertion.getCondition(),
                                    responeStatusAssertion.isContinueOnError());
                        } else if (responeStatusAssertion.getName().toLowerCase().equals("message")) {
                            checkAssertion(actualResponse.getStatusLine().getReasonPhrase().toLowerCase(),
                                    responeStatusAssertion.getValue().toLowerCase(),
                                    responeStatusAssertion.getCondition(), responeStatusAssertion.isContinueOnError());
                        }
                        responeStatusAssertion.setStatus("PASS");
                    } catch (Exception ex) {
                        responeStatusAssertion.setStatus("FAIL");
                        if (testStatus.get("FAIL") == null) {
                            testStatus.put("FAIL", 1);
                        } else {
                            testStatus.put("FAIL", testStatus.get("FAIL") + 1);
                        }
                        responeStatusAssertion.setMessage(ex.getMessage());
                        ex.printStackTrace();
                        if (!responeStatusAssertion.isContinueOnError()) {
                            throw new HaltExecution();
                        }
                    }
                }
            }
        }
        return true;
    }

    public boolean validate(ResponseAssertions assertions, Map<String, Integer> testStatus) throws Exception {
        if (assertions != null && assertions.getBody() != null) {
            for (ResponseBodyValidation assertion : assertions.getBody()) {
                try {
                    assertTest(assertion.getPath(), assertion.getCondition(), assertion.getValue(),
                            assertion.isContinueOnError());
                    assertion.setStatus("PASS");
                } catch (Exception ex) {
                    assertion.setStatus("FAIL");
                    assertion.setMessage(ex.getMessage());
                    if (testStatus.get("FAIL") == null) {
                        testStatus.put("FAIL", 1);
                    } else {
                        testStatus.put("FAIL", testStatus.get("FAIL") + 1);
                    }
                    ex.printStackTrace();
                    if (!assertion.isContinueOnError()) {
                        throw new HaltExecution(ex.getMessage());
                    }
                }
            }
        }
        return true;
    }

    public void assertTest(String path, String condition, Object value, boolean continueOnError) throws Exception {
        String valueStr = String.valueOf(value);
        checkAssertion(getAttributeValue(path), valueStr, condition, continueOnError);
    }

    public Object getAttributeValue(String path) throws Exception {
        // Ignore this method as derived classes should take care of that
        return null;
    }

    public void setAttributeValue(String path, String value) throws Exception {
        // Ignore this method as derived classes should take care of that
    }

    public String getUpdatedObjectAsString() throws Exception {
        // Ignore this method as derived classes should take care of that
        return null;
    }

    public String getMaskedResponse(List<String> template) throws Exception {
        // Ignore this method as derived classes should take care of that
        return null;
    }

    public static void checkAssertionString(String actualValue, String expectedValue, String condition,
            boolean continueOnError) throws Exception {
        if (condition == null) {
            return;
        }

        try { // Cover equalTO, present, absent, doesNotMatch, matches, contains
            if (condition.equalsIgnoreCase("equalTO")) {
                Assert.assertEquals(actualValue, expectedValue);
            } else if (condition.equalsIgnoreCase("present")) {
                Assert.assertNotNull(actualValue);
            } else if (condition.equalsIgnoreCase("absent")) {
                Assert.assertNull(actualValue);
            } else if (condition.equalsIgnoreCase("doesNotMatch")) {
                Assert.assertNotEquals(actualValue, expectedValue,
                        expectedValue + " and " + actualValue + " are same...");
            } else if (condition.equalsIgnoreCase("contains")) {
                Assert.assertTrue(actualValue.contains(expectedValue));
            } else if (condition.equalsIgnoreCase("regex")) {
                Assert.assertTrue(regexMatcher(expectedValue, actualValue));
            } else if (condition.equalsIgnoreCase("is")) {
                if (expectedValue.equalsIgnoreCase("null")) {
                    Assert.assertNull(actualValue);
                } else {
                    Assert.assertNotNull(actualValue);
                }
            } else if (condition.equalsIgnoreCase("boolean")) {
                if (expectedValue.equalsIgnoreCase("true")) {
                    Assert.assertTrue(Boolean.parseBoolean(actualValue));
                } else {
                    Assert.assertFalse(Boolean.parseBoolean(actualValue));
                }
            }
        } catch (AssertionError | Exception ex) {
            ex.printStackTrace();
            throw new Exception(ex.getMessage());
        }
    }

    public static void checkAssertion(Object actualValue, String expectedValue, String condition,
            boolean continueOnError) throws Exception {
        if (condition == null) {
            return;
        }
        if (actualValue.getClass().getName().equals("java.lang.String")
                && expectedValue.getClass().getName().equals("java.lang.String")) {
            checkAssertionString(actualValue.toString(), expectedValue.toString(), condition, continueOnError);
            return;
        }

        if (actualValue.getClass().getName().equals("java.lang.Boolean")) {
            checkAssertionString(actualValue.toString(), expectedValue.toString(), condition, continueOnError);
            return;
        }

        if (actualValue instanceof Number) {
            checkAssertionString(actualValue.toString(), expectedValue.toString(), condition, continueOnError);
            return;
        }

        try { // Cover equalTO, present, absent, doesNotMatch, matches, contains
            String[] expectedArr = expectedValue.split(",");
            String[] actualArr = getList(actualValue);
            if (condition.equalsIgnoreCase("equalTO")) {
                Assert.assertEquals(actualArr, expectedArr);
            } else if (condition.equalsIgnoreCase("contains")) {
                List<String> actualList = Arrays.asList(actualArr);
                List<String> expectedList = Arrays.asList(expectedArr);
                Assert.assertTrue(actualList.containsAll(expectedList));
            } else if (condition.equalsIgnoreCase("is")) {
                if (expectedValue.toString().equalsIgnoreCase("null")) {
                    Assert.assertNull(actualValue);
                } else {
                    Assert.assertNotNull(actualValue);
                }
            }
        } catch (AssertionError | Exception ex) {
            ex.printStackTrace();
            throw new Exception(ex.getMessage());
        }
    }

    private static String[] getList(Object value) {
        List<String> list = new ArrayList<String>();
        if (value != null && value instanceof JSONArray) {
            JSONArray jsonArr = (JSONArray) value;

            for (int i = 0; i < jsonArr.size(); i++) {
                list.add(jsonArr.get(i).toString());
            }
        }
        return list.toArray(new String[0]);
    }

    private static boolean regexMatcher(String regex, String actualValue) {
        final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
        final Matcher matcher = pattern.matcher(actualValue);
        while (matcher.find()) {
            return true;
        }
        return false;
    }

}