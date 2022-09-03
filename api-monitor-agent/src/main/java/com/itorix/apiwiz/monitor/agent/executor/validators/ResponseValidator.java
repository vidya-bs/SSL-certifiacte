package com.itorix.apiwiz.monitor.agent.executor.validators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;
import org.testng.Assert;

import net.minidev.json.JSONArray;
@Slf4j
public class ResponseValidator {

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
                    Assert.assertTrue(Boolean.getBoolean(actualValue));
                } else {
                    Assert.assertFalse(Boolean.getBoolean(actualValue));
                }
            }
        } catch (AssertionError | Exception ex) {
            log.error("Exception occurred");
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
            log.error("Exception occurred",ex);
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