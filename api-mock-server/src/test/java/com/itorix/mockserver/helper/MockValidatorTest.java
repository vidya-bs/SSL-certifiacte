package com.itorix.mockserver.helper;

import com.itorix.mockserver.common.model.expectation.*;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.*;

public class MockValidatorTest {

    @InjectMocks
    MockValidator mockValidator = new MockValidator();


    @Test
    public void checkHeaderValidation_ExpectationNameAndValueEqual() {
        Expectation expectation = getExpectation();

        MultiValueMap<String, String> actualHeader = new LinkedMultiValueMap();
        actualHeader.put("id", Arrays.asList("10"));
        assertTrue(mockValidator.checkHeader(expectation, actualHeader));

    }

    @Test
    public void checkHeaderValidation_ExpectationNameEqualValueNotEqual() {
        Expectation expectation = getExpectation();
        ArrayList<NameMultiValue> headers = new ArrayList<>();
        NameMultiValue idHeader = new NameMultiValue();
        Name idName = new Name();
        idName.setKey("id");
        idName.setCondition(Name.Condition.equalTo);
        idHeader.setName(idName);
        Value idValue = new Value();
        idValue.setCondition(Value.Condition.notEqualTo);
        idValue.setText(Arrays.asList("10"));
        idHeader.setValue(idValue);
        headers.add(idHeader);
        expectation.getRequest().setHeaders(headers);

        MultiValueMap<String, String> actualHeader = new LinkedMultiValueMap();
        actualHeader.put("JSESSIONID", Arrays.asList("11111"));
        actualHeader.put("id", Arrays.asList("11"));
        boolean checkHeader = mockValidator.checkHeader(expectation, actualHeader);
        System.out.println(  "is expectation matched ?  " + checkHeader);
        assertTrue(checkHeader);
    }


    @Test
    public void checkHeaderValidation_ExpectationNameEqualValueNotEqual_2() {
        Expectation expectation = getExpectation();
        ArrayList<NameMultiValue> headers = new ArrayList<>();
        NameMultiValue idHeader = new NameMultiValue();
        Name idName = new Name();
        idName.setKey("id");
        idName.setCondition(Name.Condition.equalTo);
        idHeader.setName(idName);
        Value idValue = new Value();
        idValue.setCondition(Value.Condition.notEqualTo);
        idValue.setText(Arrays.asList("10"));
        idHeader.setValue(idValue);
        headers.add(idHeader);
        expectation.getRequest().setHeaders(headers);

        MultiValueMap<String, String> actualHeader = new LinkedMultiValueMap();
        actualHeader.put("JSESSIONID", Arrays.asList("11111"));
        actualHeader.put("id", Arrays.asList("10"));
        boolean checkHeader = mockValidator.checkHeader(expectation, actualHeader);
        System.out.println(  "is expectation matched ?  " + checkHeader);
        assertFalse(checkHeader);
    }

    @Test
    public void checkHeaderValidation_ExpectationNameNotEqualValueNotEqual() {
        Expectation expectation = getExpectation();
        ArrayList<NameMultiValue> headers = new ArrayList<>();


        NameMultiValue idHeader = new NameMultiValue();
        Name idName = new Name();
        idName.setKey("id");
        idName.setCondition(Name.Condition.notEqualTo);
        idHeader.setName(idName);
        Value idValue = new Value();
        idValue.setCondition(Value.Condition.notEqualTo);
        idValue.setText(Arrays.asList("10"));
        idHeader.setValue(idValue);
        headers.add(idHeader);
        expectation.getRequest().setHeaders(headers);

        MultiValueMap<String, String> actualHeader = new LinkedMultiValueMap();
        actualHeader.put("JSESSIONID", Arrays.asList("11111"));
        actualHeader.put("idx", Arrays.asList("11"));
        boolean checkHeader = mockValidator.checkHeader(expectation, actualHeader);
        System.out.println(  "is header valid ?  " + checkHeader);
        assertTrue(checkHeader);

    }



    private Expectation getExpectation() {
        Expectation expectation = new Expectation();
        expectation.setName("Test");
        Request request = new Request();
        ArrayList<NameMultiValue> headers = new ArrayList<>();
        NameMultiValue idHeader = new NameMultiValue();
        Name idName = new Name();
        idName.setKey("id");
        idName.setCondition(Name.Condition.equalTo);
        idHeader.setName(idName);
        Value idValue = new Value();
        idValue.setCondition(Value.Condition.equalTo);
        idValue.setText(Arrays.asList("10"));
        idHeader.setValue(idValue);
        headers.add(idHeader);
        request.setHeaders(headers);
        expectation.setRequest(request);
        return expectation;
    }

    @Test
    public void checkMultipleHeaderExpectation() {
        Expectation expectation = getExpectation();
        NameMultiValue header = new NameMultiValue();
        Name headerName = new Name();
        headerName.setKey("name");
        headerName.setCondition(Name.Condition.equalTo);
        header.setName(headerName);
        Value nameHeaderValue = new Value();
        nameHeaderValue.setCondition(Value.Condition.equalTo);
        nameHeaderValue.setText(Arrays.asList("joe"));
        header.setValue(nameHeaderValue);
        expectation.getRequest().getHeaders().add(header);

        MultiValueMap<String, String> actualHeader = new LinkedMultiValueMap();
        actualHeader.put("name", Arrays.asList("joe"));
        actualHeader.put("id", Arrays.asList("10"));

        boolean checkHeader = mockValidator.checkHeader(expectation, actualHeader);
        System.out.println(  "is header valid ?  " + checkHeader);
        assertTrue(checkHeader);

    }

    @Test
    public void checkMultipleHeaderExpectationForNotEqualTo() {
        Expectation expectation = getExpectation();
        NameMultiValue header = new NameMultiValue();
        Name headerName = new Name();
        headerName.setKey("name");
        headerName.setCondition(Name.Condition.notEqualTo);
        header.setName(headerName);
        Value nameHeaderValue = new Value();
        nameHeaderValue.setCondition(Value.Condition.notEqualTo);
        nameHeaderValue.setText(Arrays.asList("joe"));
        header.setValue(nameHeaderValue);
        expectation.getRequest().getHeaders().add(header);

        MultiValueMap<String, String> actualHeader = new LinkedMultiValueMap();
        actualHeader.put("name1", Arrays.asList("bob"));
        actualHeader.put("id", Arrays.asList("10"));

        boolean checkHeader = mockValidator.checkHeader(expectation, actualHeader);
        System.out.println(  "is header valid ?  " + checkHeader);
        assertTrue(checkHeader);

    }
}