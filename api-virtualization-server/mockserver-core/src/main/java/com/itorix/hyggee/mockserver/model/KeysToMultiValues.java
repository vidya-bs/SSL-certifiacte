package com.itorix.hyggee.mockserver.model;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import com.itorix.hyggee.mockserver.collections.CaseInsensitiveRegexMultiMap;

import org.apache.commons.lang3.ArrayUtils;

import static com.itorix.hyggee.mockserver.model.NottableString.*;

import java.util.*;

/**
 *   
 */
public abstract class KeysToMultiValues<T extends KeyToMultiValue, K extends KeysToMultiValues> extends ObjectWithJsonToString {

    private ListMultimap<NottableString, NottableString> listMultimap = Multimaps.synchronizedListMultimap(LinkedListMultimap.<NottableString, NottableString>create());

    public CaseInsensitiveRegexMultiMap toCaseInsensitiveRegexMultiMap(List<T> entries) {
        CaseInsensitiveRegexMultiMap caseInsensitiveRegexMultiMap = new CaseInsensitiveRegexMultiMap();
        if (entries != null) {
            for (KeyToMultiValue keyToMultiValue : entries) {
                for (NottableString value : keyToMultiValue.getValues()) {
                    caseInsensitiveRegexMultiMap.put(keyToMultiValue.getName(), value);
                }
            }
        }
        return caseInsensitiveRegexMultiMap;
    }

    public abstract T build(NottableString name, List<NottableString> values);

    public K withEntries(Map<String, List<String>> entries) {
        listMultimap.clear();
        for (String name : entries.keySet()) {
            for (String value : entries.get(name)) {
                withEntry(name, value);
            }
        }
        return (K) this;
    }

    public K withEntries(List<T> entries) {
        listMultimap.clear();
        if (entries != null) {
            for (T entry : entries) {
                withEntry(entry);
            }
        }
        return (K) this;
    }

    @SafeVarargs
    public final K withEntries(T... entries) {
        if (ArrayUtils.isNotEmpty(entries)) {
            withEntries(Arrays.asList(entries));
        }
        return (K) this;
    }

    public K withEntry(T entry) {
        if (entry.getValues().isEmpty()) {
            listMultimap.put(entry.getName(), null);
        } else {
            listMultimap.putAll(entry.getName(), entry.getValues());
        }
        return (K) this;
    }

    public K withEntry(String name, String... values) {
        if (ArrayUtils.isNotEmpty(values)) {
            listMultimap.putAll(string(name), deserializeNottableStrings(values));
        }
        return (K) this;
    }

    public K withEntry(NottableString name, List<NottableString> values) {
        if (values != null) {
            listMultimap.putAll(name, values);
        }
        return (K) this;
    }

    public K withEntry(NottableString name, NottableString... values) {
        if (ArrayUtils.isNotEmpty(values)) {
            withEntry(name, Arrays.asList(values));
        }
        return (K) this;
    }

    public K remove(String name) {
        for (NottableString key : new ArrayList<>(listMultimap.keySet())) {
            if (key.equalsIgnoreCase(name)) {
                listMultimap.removeAll(key);
            }
        }
        return (K) this;
    }

    public K remove(NottableString name) {
        for (NottableString key : new ArrayList<>(listMultimap.keySet())) {
            if (key.equalsIgnoreCase(name)) {
                listMultimap.removeAll(key);
            }
        }
        return (K) this;
    }

    public K replaceEntry(T entry) {
        if (entry != null) {
            remove(entry.getName());
            listMultimap.putAll(entry.getName(), entry.getValues());
        }
        return (K) this;
    }

    public K replaceEntry(String name, String... values) {
        if (ArrayUtils.isNotEmpty(values)) {
            remove(name);
            listMultimap.putAll(string(name), deserializeNottableStrings(values));
        }
        return (K) this;
    }

    public List<T> getEntries() {
        ArrayList<T> headers = new ArrayList<>();
        for (NottableString nottableString : new ArrayList<>(listMultimap.keySet())) {
            headers.add(build(nottableString, listMultimap.get(nottableString)));
        }
        return headers;
    }

    public List<String> getValues(String name) {
        List<String> values = new ArrayList<>();
        for (NottableString key : new ArrayList<>(listMultimap.keySet())) {
            if (key != null &&
                name != null &&
                key.equalsIgnoreCase(name)) {
                values.addAll(serialiseNottableString(listMultimap.get(key)));
            }
        }
        return values;
    }

    public String getFirstValue(String name) {
        String firstEntryValue = "";
        List<String> values = getValues(name);
        if (!values.isEmpty()) {
            firstEntryValue = values.get(0);
        }
        return firstEntryValue;
    }

    public boolean containsEntry(String name) {
        if (!getValues(name).isEmpty()) {
            return true;
        }
        return false;
    }

    public boolean containsEntry(String name, String value) {
        return containsEntry(string(name), string(value));
    }

    public boolean containsEntry(NottableString name, NottableString value) {
        for (NottableString entryKey : new ArrayList<>(listMultimap.keySet())) {
            if (entryKey != null &&
                name != null &&
                entryKey.equalsIgnoreCase(name)) {
                if (listMultimap.get(entryKey) != null &&
                    value != null) {
                    for (NottableString entryValue : new ArrayList<>(listMultimap.get(entryKey))) {
                        if (value.equalsIgnoreCase(entryValue)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public CaseInsensitiveRegexMultiMap toCaseInsensitiveRegexMultiMap() {
        return toCaseInsensitiveRegexMultiMap(this.getEntries());
    }

    public boolean isEmpty() {
        return listMultimap.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof KeysToMultiValues)) {
            return false;
        }
        KeysToMultiValues<?, ?> that = (KeysToMultiValues<?, ?>) o;
        return Objects.equals(listMultimap, that.listMultimap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(listMultimap);
    }
}
