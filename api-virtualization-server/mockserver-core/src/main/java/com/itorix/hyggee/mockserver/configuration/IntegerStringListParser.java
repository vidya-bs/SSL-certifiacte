package com.itorix.hyggee.mockserver.configuration;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.itorix.hyggee.mockserver.logging.MockServerLogger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *   
 */
public class IntegerStringListParser {

    private MockServerLogger mockServerLogger = new MockServerLogger(IntegerStringListParser.class);

    public Integer[] toArray(String integers) {
        List<Integer> integerList = toList(integers);
        return integerList.toArray(new Integer[integerList.size()]);
    }

    public List<Integer> toList(String integers) {
        List<Integer> integerList = new ArrayList<Integer>();
        for (String integer : Splitter.on(",").split(integers)) {
            try {
                integerList.add(Integer.parseInt(integer.trim()));
            } catch (NumberFormatException nfe) {
                mockServerLogger.error("NumberFormatException converting " + integer + " to integer", nfe);
            }
        }
        return integerList;
    }

    public String toString(Integer[] integers) {
        return toString(Arrays.asList(integers));
    }

    public String toString(List<Integer> integers) {
        return Joiner.on(",").join(integers);
    }
}
