package com.itorix.hyggee.mockserver.client.serialization.model;

import com.itorix.hyggee.mockserver.matchers.Times;
import com.itorix.hyggee.mockserver.model.ObjectWithReflectiveEqualsHashCodeToString;

/**
 *   
 */
public class TimesDTO extends ObjectWithReflectiveEqualsHashCodeToString implements DTO<Times> {

    private int remainingTimes;
    private boolean unlimited;

    public TimesDTO(Times times) {
        remainingTimes = times.getRemainingTimes();
        unlimited = times.isUnlimited();
    }

    public TimesDTO() {
    }

    public Times buildObject() {
        if (unlimited) {
            return Times.unlimited();
        } else {
            return Times.exactly(remainingTimes);
        }
    }

    public int getRemainingTimes() {
        return remainingTimes;
    }

    public boolean isUnlimited() {
        return unlimited;
    }
}
