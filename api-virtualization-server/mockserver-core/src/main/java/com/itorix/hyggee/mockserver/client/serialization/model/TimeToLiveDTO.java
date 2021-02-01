package com.itorix.hyggee.mockserver.client.serialization.model;

import com.itorix.hyggee.mockserver.matchers.TimeToLive;
import com.itorix.hyggee.mockserver.model.ObjectWithReflectiveEqualsHashCodeToString;

import java.util.concurrent.TimeUnit;

/**
 *   
 */
public class TimeToLiveDTO extends ObjectWithReflectiveEqualsHashCodeToString implements DTO<TimeToLive> {

    private TimeUnit timeUnit;
    private Long timeToLive;
    private boolean unlimited;

    public TimeToLiveDTO(TimeToLive timeToLive) {
        this.timeUnit = timeToLive.getTimeUnit();
        this.timeToLive = timeToLive.getTimeToLive();
        this.unlimited = timeToLive.isUnlimited();
    }

    public TimeToLiveDTO() {
    }


    public TimeToLive buildObject() {
        if (unlimited) {
            return TimeToLive.unlimited();
        } else {
            return TimeToLive.exactly(timeUnit, timeToLive);
        }
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public Long getTimeToLive() {
        return timeToLive;
    }

    public boolean isUnlimited() {
        return unlimited;
    }
}
