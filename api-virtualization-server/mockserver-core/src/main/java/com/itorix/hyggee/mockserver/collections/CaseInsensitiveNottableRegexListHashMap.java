package com.itorix.hyggee.mockserver.collections;

import com.itorix.hyggee.mockserver.matchers.RegexStringMatcher;
import com.itorix.hyggee.mockserver.model.NottableString;

import static com.itorix.hyggee.mockserver.model.NottableString.string;

import java.util.*;

/**
 * Map that uses case insensitive regex expression matching for keys and values
 *
 *   
 */
class CaseInsensitiveNottableRegexListHashMap extends LinkedHashMap<NottableString, List<NottableString>> implements Map<NottableString, List<NottableString>> {

    @Override
    public synchronized boolean containsKey(Object key) {
        boolean result = false;

        if (key instanceof NottableString) {
            if (super.containsKey(key)) {
                result = true;
            } else {
                for (NottableString keyToCompare : keySet()) {
                    if (RegexStringMatcher.matches((NottableString) key, keyToCompare, true)) {
                        result = true;
                        break;
                    }
                }
            }
        } else if (key instanceof String) {
            result = containsKey(string((String) key));
        }

        return result;
    }

    @Override
    public synchronized List<NottableString> get(Object key) {
        if (key instanceof NottableString) {
            for (Entry<NottableString, List<NottableString>> entry : entrySet()) {
                if (RegexStringMatcher.matches((NottableString) key, entry.getKey(), true)) {
                    return entry.getValue();
                }
            }
        } else if (key instanceof String) {
            return get(string((String) key));
        }
        return null;
    }

    public synchronized Collection<List<NottableString>> getAll(Object key) {
        List<List<NottableString>> values = new ArrayList<List<NottableString>>();
        if (key instanceof NottableString) {
            for (Entry<NottableString, List<NottableString>> entry : entrySet()) {
                if (RegexStringMatcher.matches((NottableString) key, entry.getKey(), true)) {
                    values.add(entry.getValue());
                }
            }
        } else if (key instanceof String) {
            return getAll(string((String) key));
        }
        return values;
    }

    public synchronized List<NottableString> put(String key, List<NottableString> value) {
        return super.put(string(key), value);
    }

    @Override
    public synchronized List<NottableString> remove(Object key) {
        List<NottableString> values = new ArrayList<NottableString>();
        if (key instanceof NottableString) {
            for (Entry<NottableString, List<NottableString>> entry : new HashSet<Map.Entry<NottableString, List<NottableString>>>(entrySet())) {
                if (RegexStringMatcher.matches((NottableString) key, entry.getKey(), true)) {
                    values.addAll(super.remove(entry.getKey()));
                }
            }
        } else if (key instanceof String) {
            return remove(string((String) key));
        }
        return values;
    }
}
