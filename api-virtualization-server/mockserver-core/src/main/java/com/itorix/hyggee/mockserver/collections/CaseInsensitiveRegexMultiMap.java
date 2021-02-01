package com.itorix.hyggee.mockserver.collections;

import org.apache.commons.lang3.builder.EqualsBuilder;

import com.itorix.hyggee.mockserver.matchers.RegexStringMatcher;
import com.itorix.hyggee.mockserver.model.NottableString;
import com.itorix.hyggee.mockserver.model.ObjectWithReflectiveEqualsHashCodeToString;

import static com.itorix.hyggee.mockserver.model.NottableString.string;
import static com.itorix.hyggee.mockserver.model.NottableString.strings;

import java.util.*;

/**
 * MultiMap that uses case insensitive regex expression matching for keys and values
 *
 *   
 */
public class CaseInsensitiveRegexMultiMap extends ObjectWithReflectiveEqualsHashCodeToString implements Map<NottableString, NottableString> {
    private final CaseInsensitiveNottableRegexListHashMap backingMap = new CaseInsensitiveNottableRegexListHashMap();

    public static CaseInsensitiveRegexMultiMap multiMap(String[]... keyAndValues) {
        CaseInsensitiveRegexMultiMap multiMap = new CaseInsensitiveRegexMultiMap();
        for (String[] keyAndValue : keyAndValues) {
            for (int i = 1; i < keyAndValue.length; i++) {
                multiMap.put(keyAndValue[0], keyAndValue[i]);
            }
        }
        return multiMap;
    }

    public static CaseInsensitiveRegexMultiMap multiMap(NottableString[]... keyAndValues) {
        CaseInsensitiveRegexMultiMap multiMap = new CaseInsensitiveRegexMultiMap();
        for (NottableString[] keyAndValue : keyAndValues) {
            for (int i = 1; i < keyAndValue.length; i++) {
                multiMap.put(keyAndValue[0], keyAndValue[i]);
            }
        }
        return multiMap;
    }

    public static Entry<NottableString, NottableString> entry(String key, String value) {
        return new ImmutableEntry(key, value);
    }

    public boolean containsAll(CaseInsensitiveRegexMultiMap subSet) {
        if (size() == 0 && subSet.allKeysNotted()) {
            return true;
        } else {
            for (Entry<NottableString, NottableString> entry : subSet.entryList()) {
                if ((entry.getKey().isNot() || entry.getValue().isNot()) && containsKeyValue(entry.getKey().getValue(), entry.getValue().getValue())) {
                    return false;
                } else if (!containsKeyValue(entry.getKey(), entry.getValue())) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean allKeysNotted() {
        for (NottableString key : keySet()) {
            if (!key.isNot()) {
                return false;
            }
        }
        return true;
    }

    public synchronized boolean containsKeyValue(String key, String value) {
        return containsKeyValue(string(key), string(value));
    }

    public synchronized boolean containsKeyValue(NottableString key, NottableString value) {
        boolean result = false;

        for (Entry<NottableString, NottableString> matcherEntry : entryList()) {
            if (RegexStringMatcher.matches(value, matcherEntry.getValue(), true)
                && RegexStringMatcher.matches(key, matcherEntry.getKey(), true)) {
                result = true;
                break;
            } else {
                result = false;
            }
        }

        return result;
    }

    @Override
    public synchronized boolean containsValue(Object value) {
        if (value instanceof NottableString) {
            for (NottableString key : backingMap.keySet()) {
                for (List<NottableString> allKeyValues : backingMap.getAll(key)) {
                    for (NottableString keyValue : allKeyValues) {
                        if (RegexStringMatcher.matches(keyValue, (NottableString) value, false)) {
                            return true;
                        }
                    }
                }
            }
        } else if (value instanceof String) {
            return containsValue(string((String) value));
        }
        return false;
    }

    @Override
    public synchronized boolean containsKey(Object key) {
        return backingMap.containsKey(key);
    }

    @Override
    public synchronized NottableString get(Object key) {
        if (key instanceof String) {
            return get(string((String) key));
        } else {
            List<NottableString> values = backingMap.get(key);
            if (values != null && values.size() > 0) {
                return values.get(0);
            } else {
                return null;
            }
        }
    }

    public synchronized List<NottableString> getAll(String key) {
        return getAll(string(key));
    }

    public synchronized List<NottableString> getAll(NottableString key) {
        List<NottableString> all = new ArrayList<NottableString>();
        for (List<NottableString> subList : backingMap.getAll(key)) {
            all.addAll(subList);
        }
        return all;
    }

    public synchronized NottableString put(String key, String value) {
        return put(string(key), string(value));
    }

    @Override
    public synchronized NottableString put(NottableString key, NottableString value) {
        List<NottableString> list = Collections.synchronizedList(new ArrayList<NottableString>());
        for (Entry<NottableString, NottableString> entry : entryList()) {
            if (EqualsBuilder.reflectionEquals(entry.getKey(), key)) {
                list.add(entry.getValue());
            }
        }
        list.add(value);
        backingMap.put(key, list);
        return value;
    }

    public synchronized List<NottableString> put(String key, List<String> values) {
        return put(string(key), strings(values));
    }

    public synchronized List<NottableString> put(NottableString key, List<NottableString> values) {
        if (containsKey(key)) {
            for (NottableString value : values) {
                put(key, value);
            }
        } else {
            backingMap.put(key, values);
        }
        return values;
    }

    public void putValuesForNewKeys(CaseInsensitiveRegexMultiMap multiMap) {
        for (NottableString key : multiMap.keySet()) {
            if (!containsKey(key)) {
                backingMap.put(key, multiMap.getAll(key));
            }
        }
    }

    @Override
    public synchronized NottableString remove(Object key) {
        if (key instanceof String) {
            return remove(string((String) key));
        } else {
            List<NottableString> values = backingMap.get(key);
            if (values != null && values.size() > 0) {
                NottableString removed = values.remove(0);
                if (values.size() == 0) {
                    backingMap.remove(key);
                }
                return removed;
            } else {
                return null;
            }
        }
    }

    public synchronized List<NottableString> removeAll(NottableString key) {
        return backingMap.remove(key);
    }

    public synchronized List<NottableString> removeAll(String key) {
        return backingMap.remove(key);
    }

    @Override
    public synchronized void putAll(Map<? extends NottableString, ? extends NottableString> map) {
        for (Entry<? extends NottableString, ? extends NottableString> entry : map.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public synchronized void clear() {
        backingMap.clear();
    }

    @Override
    public synchronized Set<NottableString> keySet() {
        return backingMap.keySet();
    }

    @Override
    public synchronized Collection<NottableString> values() {
        Collection<NottableString> values = new ArrayList<NottableString>();
        for (List<NottableString> valuesForKey : backingMap.values()) {
            values.addAll(valuesForKey);
        }
        return values;
    }

    @Override
    public synchronized Set<Entry<NottableString, NottableString>> entrySet() {
        Set<Entry<NottableString, NottableString>> entrySet = new LinkedHashSet<Entry<NottableString, NottableString>>();
        for (Entry<NottableString, List<NottableString>> entry : backingMap.entrySet()) {
            for (NottableString value : entry.getValue()) {
                entrySet.add(new ImmutableEntry(entry.getKey(), value));
            }
        }
        return entrySet;
    }

    public synchronized List<Entry<NottableString, NottableString>> entryList() {
        List<Entry<NottableString, NottableString>> entrySet = new ArrayList<Entry<NottableString, NottableString>>();
        for (Entry<NottableString, List<NottableString>> entry : backingMap.entrySet()) {
            for (NottableString value : entry.getValue()) {
                entrySet.add(new ImmutableEntry(entry.getKey(), value));
            }
        }
        return entrySet;
    }

    @Override
    public synchronized int size() {
        return backingMap.size();
    }

    @Override
    public synchronized boolean isEmpty() {
        return backingMap.isEmpty();
    }

    static class ImmutableEntry extends ObjectWithReflectiveEqualsHashCodeToString implements Entry<NottableString, NottableString> {
        private final NottableString key;
        private final NottableString value;

        ImmutableEntry(String key, String value) {
            this.key = string(key);
            this.value = string(value);
        }

        ImmutableEntry(NottableString key, NottableString value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public NottableString getKey() {
            return key;
        }

        @Override
        public NottableString getValue() {
            return value;
        }

        @Override
        public NottableString setValue(NottableString value) {
            throw new UnsupportedOperationException("ImmutableEntry is immutable");
        }
    }

}



