package com.itorix.hyggee.mockserver.client.serialization.model;

import com.itorix.hyggee.mockserver.model.ObjectWithReflectiveEqualsHashCodeToString;
import com.itorix.hyggee.mockserver.verify.VerificationTimes;

/**
 *   
 */
public class VerificationTimesDTO extends ObjectWithReflectiveEqualsHashCodeToString implements DTO<VerificationTimes> {

    private int count;
    private boolean exact;

    public VerificationTimesDTO(VerificationTimes times) {
        count = times.getCount();
        exact = times.isExact();
    }

    public VerificationTimesDTO() {
    }

    public VerificationTimes buildObject() {
        if (!exact) {
            return VerificationTimes.atLeast(count);
        } else if (count == 1) {
            return VerificationTimes.once();
        } else {
            return VerificationTimes.exactly(count);
        }
    }

    public int getCount() {
        return count;
    }

    public boolean isExact() {
        return exact;
    }
}
