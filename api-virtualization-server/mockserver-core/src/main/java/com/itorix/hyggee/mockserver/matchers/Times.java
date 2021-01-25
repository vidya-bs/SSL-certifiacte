package com.itorix.hyggee.mockserver.matchers;

import com.itorix.hyggee.mockserver.model.ObjectWithReflectiveEqualsHashCodeToString;

/**
 *   
 */
public class Times extends ObjectWithReflectiveEqualsHashCodeToString {

    private int remainingTimes;
    private final boolean unlimited;

    private Times(int remainingTimes, boolean unlimited) {
        this.remainingTimes = remainingTimes;
        this.unlimited = unlimited;
    }

    public static Times unlimited() {
        return new Times(0, true);
    }

    public static Times once() {
        return new Times(1, false);
    }

    public static Times exactly(int count) {
        return new Times(count, false);
    }

    public int getRemainingTimes() {
        return remainingTimes;
    }

    public boolean isUnlimited() {
        return unlimited;
    }

    public boolean greaterThenZero() {
        if (unlimited || remainingTimes > 0) {
            return true;
        } else {
            return false;
        }
    }

    public Times decrement() {
        if (!unlimited) {
            remainingTimes--;
        }
        return this;
    }

    public Times clone() {
        if (unlimited) {
            return Times.unlimited();
        } else {
            return Times.exactly(remainingTimes);
        }
    }
}
