package com.foldik.versioning;

import java.util.*;

public class OrderedProperties extends Properties {

    private final Set<Object> keys = new LinkedHashSet<>();

    public OrderedProperties() {
    }

    public List<Object> orderedKeys() {
        return Collections.list(keys());
    }

    public Enumeration<Object> keys() {
        return Collections.enumeration(keys);
    }

    public Object put(Object key, Object value) {
        keys.add(key);
        return super.put(key, value);
    }
}