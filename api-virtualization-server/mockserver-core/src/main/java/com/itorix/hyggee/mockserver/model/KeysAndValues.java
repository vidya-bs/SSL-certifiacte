package com.itorix.hyggee.mockserver.model;

import static com.itorix.hyggee.mockserver.model.NottableString.string;

import java.util.*;

import com.itorix.hyggee.mockserver.collections.CaseInsensitiveRegexHashMap;

/**
 *   
 */
public abstract class KeysAndValues<T extends KeyAndValue, K extends KeysAndValues> extends ObjectWithJsonToString {

    private Map<NottableString, NottableString> map = new LinkedHashMap<>();

    public CaseInsensitiveRegexHashMap toCaseInsensitiveRegexMultiMap(List<T> entries) {
        CaseInsensitiveRegexHashMap caseInsensitiveRegexHashMap = new CaseInsensitiveRegexHashMap();
        if (entries != null) {
            for (KeyAndValue keyToMultiValue : entries) {
                caseInsensitiveRegexHashMap.put(keyToMultiValue.getName(), keyToMultiValue.getValue());
            }
        }
        return caseInsensitiveRegexHashMap;
    }

    public abstract T build(NottableString name, NottableString value);

    public K withEntries(List<T> cookies) {
        map.clear();
        if (cookies != null) {
            for (T cookie : cookies) {
                withEntry(cookie);
            }
        }
        return (K) this;
    }

    public K withEntries(T... cookies) {
        if (cookies != null) {
            withEntries(Arrays.asList(cookies));
        }
        return (K) this;
    }

    public K withEntry(T cookie) {
        map.put(cookie.getName(), cookie.getValue());
        return (K) this;
    }

    public K withEntry(String name, String value) {
        map.put(string(name), string(value));
        return (K) this;
    }

    public K withEntry(NottableString name, NottableString value) {
        map.put(name, value);
        return (K) this;
    }

    public List<T> getEntries() {
        ArrayList<T> cookies = new ArrayList<>();
        for (NottableString nottableString : map.keySet()) {
            cookies.add(build(nottableString, map.get(nottableString)));
        }
        return cookies;
    }

    public CaseInsensitiveRegexHashMap toCaseInsensitiveRegexMultiMap() {
        return toCaseInsensitiveRegexMultiMap(this.getEntries());
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }
}
