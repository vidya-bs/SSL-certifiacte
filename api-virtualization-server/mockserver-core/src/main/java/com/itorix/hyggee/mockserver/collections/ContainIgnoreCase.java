package com.itorix.hyggee.mockserver.collections;

import java.util.Set;

/**
 *   
 */
public class ContainIgnoreCase {

    static public boolean containsIgnoreCase(Set<String> set, String value) {
        for (String entry : set) {
            if (entry.equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }
}
