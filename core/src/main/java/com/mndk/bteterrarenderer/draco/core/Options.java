package com.mndk.bteterrarenderer.draco.core;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class Options {

    private final Map<String, String> options = new HashMap<>();

    public void mergeAndReplace(Options otherOptions) {
        options.putAll(otherOptions.options);
    }

    public void setInt(String name, int val) {
        options.put(name, Integer.toString(val));
    }

    public void setFloat(String name, float val) {
        options.put(name, Float.toString(val));
    }

    public void setBool(String name, boolean val) {
        options.put(name, val ? "1" : "0");
    }

    public void setString(String name, String val) {
        options.put(name, val);
    }

    public <S, V extends VectorD<S, V>> void setVector(String name, V vec) {
        setVector(name, vec.getDataType(), vec.getDimension(), vec::get);
    }

    public <T> void setVector(String name, DataType<T> dataType, int numDims, Function<Integer, T> vec) {
        StringBuilder out = new StringBuilder();
        for(int i = 0; i < numDims; i++) {
            if(i > 0) out.append(" ");
            out.append(dataType.toString(vec.apply(i)));
        }
        options.put(name, out.toString());
    }

    public int getInt(String name) {
        return getInt(name, -1);
    }

    public int getInt(String name, int defaultVal) {
        String value = options.get(name);
        return value == null ? defaultVal : Integer.parseInt(value);
    }

    public float getFloat(String name) {
        return getFloat(name, -1);
    }

    public float getFloat(String name, float defaultVal) {
        String value = options.get(name);
        return value == null ? defaultVal : Float.parseFloat(value);
    }

    public boolean getBool(String name) {
        return getBool(name, false);
    }

    public boolean getBool(String name, boolean defaultVal) {
        String value = options.get(name);
        return value == null ? defaultVal : Integer.parseInt(value) != 0;
    }

    public String getString(String name) {
        return getString(name, "");
    }

    public String getString(String name, String defaultVal) {
        String value = options.get(name);
        return value == null ? defaultVal : value;
    }

    public <S, V extends VectorD<S, V>> V getVector(String name, V outVec) {
        getVector(name, outVec.getDataType(), outVec.getDimension(), outVec::set);
        return outVec;
    }

    public <T> boolean getVector(String name, DataType<T> dataType, int numDims, BiConsumer<Integer, T> outVal) {
        String value = options.get(name);
        if(value == null) return false;
        String[] parts = value.split(" ");
        for(int i = 0; i < numDims; i++) {
            if(i >= parts.length) return true;
            outVal.accept(i, dataType.parse(parts[i]));
        }
        return true;
    }

    public boolean isOptionSet(String name) {
        return options.containsKey(name);
    }

}
