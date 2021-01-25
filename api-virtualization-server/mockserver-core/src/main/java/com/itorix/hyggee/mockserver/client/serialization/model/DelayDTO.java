package com.itorix.hyggee.mockserver.client.serialization.model;

import java.util.concurrent.TimeUnit;

import com.itorix.hyggee.mockserver.model.Delay;
import com.itorix.hyggee.mockserver.model.ObjectWithReflectiveEqualsHashCodeToString;

/**
 *   
 */
public class DelayDTO extends ObjectWithReflectiveEqualsHashCodeToString implements DTO<Delay> {

    private TimeUnit timeUnit;
    private long value;

    public DelayDTO(Delay delay) {
        if (delay != null) {
            timeUnit = delay.getTimeUnit();
            value = delay.getValue();
        }
    }

    public DelayDTO() {
    }

    public Delay buildObject() {
        return new Delay(timeUnit, value);
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public DelayDTO setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
        return this;
    }

    public long getValue() {
        return value;
    }

    public DelayDTO setValue(long value) {
        this.value = value;
        return this;
    }
}
