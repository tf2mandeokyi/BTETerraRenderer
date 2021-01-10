package com.mndk.kmap4bte.util;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Deprecated
public class IndexedMap<K, V> extends ArrayList<Map.Entry<K, V>> {

    public int getIndexByKey(K key) {
        for(int i = 0; i < size(); i++) {
            if(get(i).getKey().equals(key)) {
                return i;
            }
        }
        return -1;
    }

    public K getKeyByIndex(int index) {
        return get(index).getKey();
    }

    public int getIndexByValue(V value) {
        for(int i = 0; i < size(); i++) {
            if(get(i).getValue().equals(value)) {
                return i;
            }
        }
        return -1;
    }

    public V getValueByIndex(int index) {
        return get(index).getValue();
    }

    public boolean containsKey(K key) {
        return getIndexByKey(key) != -1;
    }

    public boolean containsValue(V value) {
        return getIndexByValue(value) != -1;
    }

    public V getValueByKey(K key) {
        for(int i = 0; i < size(); i++) {
            if(get(i).getKey().equals(key)) {
                return get(i).getValue();
            }
        }
        return null;
    }

    public Map.Entry<K, V> getEntryByKey(K key) {
        for(int i = 0; i < size(); i++) {
            if(get(i).getKey().equals(key)) {
                return get(i);
            }
        }
        return null;
    }

    public void put(K key, V value) {
        if(containsKey(key)) {
            for(int i = 0; i < size(); i++) {
                if(get(i).getKey().equals(key)) {
                    get(i).setValue(value);
                }
            }
        }
        else {
            this.add(new AbstractMap.SimpleEntry<K, V>(key, value));
        }
    }

    public void putAll(Map<K, V> m) {
        this.addAll(m.entrySet());
    }

    public List<K> keys() {
        List<K> result = new ArrayList<>();
        for(int i = 0; i < size(); i++) {
            result.add(get(i).getKey());
        }
        return result;
    }

    public List<V> values() {
        List<V> result = new ArrayList<>();
        for(int i = 0; i < size(); i++) {
            result.add(get(i).getValue());
        }
        return result;
    }
}
