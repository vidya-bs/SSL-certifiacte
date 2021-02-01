package com.itorix.hyggee.mockserver.collections;

import java.util.*;

import com.itorix.hyggee.mockserver.model.ObjectWithReflectiveEqualsHashCodeToString;

/**
 *   
 */
public class CircularMultiMap<K, V> implements Map<K, V> {
    private final int maxValuesPerKeySize;
    private final Map<K, List<V>> backingMap;

    public CircularMultiMap(int maxNumberOfKeys, int maxNumberOfValuesPerKey) {
        this.maxValuesPerKeySize = maxNumberOfValuesPerKey;
        backingMap = Collections.synchronizedMap(new CircularHashMap<K, List<V>>(maxNumberOfKeys));
    }

    @Override
    public synchronized int size() {
        return backingMap.size();
    }

    @Override
    public synchronized boolean isEmpty() {
        return backingMap.isEmpty();
    }

    @Override
    public synchronized boolean containsKey(Object key) {
        return backingMap.containsKey(key);
    }

    @Override
    public synchronized boolean containsValue(Object value) {
        for (Entry<K, List<V>> entry : backingMap.entrySet()) {
            if (entry.getValue().contains(value)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public synchronized V get(Object key) {
        List<V> values = backingMap.get(key);
        if (values != null && values.size() > 0) {
            return values.get(0);
        } else {
            return null;
        }
    }

    public synchronized List<V> getAll(Object key) {
        List<V> values = backingMap.get(key);
        if (values == null) {
            values = new ArrayList<V>();
        }
        return values;
    }

    public synchronized List<K> getKeyListForValues() {
        List<K> values = new ArrayList<K>();
        for (K key : backingMap.keySet()) {
            for (V value : backingMap.get(key)) {
                values.add(key);
            }
        }
        return values;
    }

    @Override
    public synchronized V put(K key, V value) {
        if (containsKey(key)) {
            backingMap.get(key).add(value);
        } else {
            List<V> list = Collections.synchronizedList(new CircularLinkedList<V>(maxValuesPerKeySize));
            list.add(value);
            backingMap.put(key, list);
        }
        return value;
    }

    @Override
    public synchronized V remove(Object key) {
        List<V> values = backingMap.get(key);
        if (values != null && values.size() > 0) {
            return values.remove(0);
        } else {
            return null;
        }
    }

    public synchronized List<V> removeAll(Object key) {
        return backingMap.remove(key);
    }

    @Override
    public synchronized void putAll(Map<? extends K, ? extends V> map) {
        for (Entry<? extends K, ? extends V> entry : map.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public synchronized void clear() {
        backingMap.clear();
    }

    @Override
    public synchronized Set<K> keySet() {
        return backingMap.keySet();
    }

    @Override
    public synchronized Collection<V> values() {
        Collection<V> values = new ArrayList<V>();
        for (List<V> valuesForKey : backingMap.values()) {
            values.addAll(valuesForKey);
        }
        return values;
    }

    @Override
    public synchronized Set<Entry<K, V>> entrySet() {
        Set<Entry<K, V>> entrySet = new LinkedHashSet<Entry<K, V>>();
        for (Entry<K, List<V>> entry : backingMap.entrySet()) {
            for (V value : entry.getValue()) {
                entrySet.add(new ImmutableEntry(entry.getKey(), value));
            }
        }
        return entrySet;
    }

    class ImmutableEntry extends ObjectWithReflectiveEqualsHashCodeToString implements Entry<K, V> {
        private final K key;
        private final V value;

        ImmutableEntry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            throw new UnsupportedOperationException("ImmutableEntry is immutable");
        }
    }

}



