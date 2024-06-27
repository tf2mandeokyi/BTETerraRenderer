package com.mndk.bteterrarenderer.draco.compression.config;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.draco.core.Options;
import com.mndk.bteterrarenderer.draco.core.VectorD;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

public class DracoOptions<AttKeyT> {

    @Getter @Setter
    private Options globalOptions = new Options();
    private final Map<AttKeyT, Options> attributeOptions = new HashMap<>();

    public int getAttributeInt(AttKeyT attKey, String name, int defaultVal) {
        return this.attributeOrGlobalOptions(attKey, name).getInt(name, defaultVal);
    }

    public void setAttributeInt(AttKeyT attKey, String name, int val) {
        this.getAttributeOptions(attKey).setInt(name, val);
    }

    public float getAttributeFloat(AttKeyT attKey, String name, float defaultVal) {
        return this.attributeOrGlobalOptions(attKey, name).getFloat(name, defaultVal);
    }

    public void setAttributeFloat(AttKeyT attKey, String name, float val) {
        this.getAttributeOptions(attKey).setFloat(name, val);
    }

    public boolean getAttributeBool(AttKeyT attKey, String name, boolean defaultVal) {
        return this.attributeOrGlobalOptions(attKey, name).getBool(name, defaultVal);
    }

    public void setAttributeBool(AttKeyT attKey, String name, boolean val) {
        this.getAttributeOptions(attKey).setBool(name, val);
    }

    public <S, SArray, V extends VectorD<S, SArray, V>> V getAttributeVector(AttKeyT attKey, String name, V outVec) {
        return this.attributeOrGlobalOptions(attKey, name).getVector(name, outVec);
    }

    public <S, SArray, V extends VectorD<S, SArray, V>> void setAttributeVector(AttKeyT attKey, String name, V val) {
        this.getAttributeOptions(attKey).setVector(name, val);
    }

    public <T, TArray>
    boolean getAttributeVector(AttKeyT attKey, String name, DataType<T, TArray> dataType, int numDims, TArray val) {
        return this.attributeOrGlobalOptions(attKey, name).getVector(name, dataType, numDims, val);
    }

    public <T, TArray>
    void setAttributeVector(AttKeyT attKey, String name, DataType<T, TArray> dataType, int numDims, TArray val) {
        this.getAttributeOptions(attKey).setVector(name, dataType, numDims, val);
    }

    public boolean isAttributeOptionSet(AttKeyT attKey, String name) {
        return this.attributeOrGlobalOptions(attKey, name).isOptionSet(name);
    }

    public int getGlobalInt(String name, int defaultVal) {
        return globalOptions.getInt(name, defaultVal);
    }

    public void setGlobalInt(String name, int val) {
        globalOptions.setInt(name, val);
    }

    public float getGlobalFloat(String name, float defaultVal) {
        return globalOptions.getFloat(name, defaultVal);
    }

    public void setGlobalFloat(String name, float val) {
        globalOptions.setFloat(name, val);
    }

    public boolean getGlobalBool(String name, boolean defaultVal) {
        return globalOptions.getBool(name, defaultVal);
    }

    public void setGlobalBool(String name, boolean val) {
        globalOptions.setBool(name, val);
    }

    public <S, SArray, V extends VectorD<S, SArray, V>> V getGlobalVector(String name, V outVec) {
        return globalOptions.getVector(name, outVec);
    }

    public <S, SArray, V extends VectorD<S, SArray, V>> void setGlobalVector(String name, V val) {
        globalOptions.setVector(name, val);
    }

    public <T, TArray>
    boolean getGlobalVector(String name, DataType<T, TArray> dataType, int numDims, TArray val) {
        return globalOptions.getVector(name, dataType, numDims, val);
    }

    public <T, TArray>
    void setGlobalVector(String name, DataType<T, TArray> dataType, int numDims, TArray val) {
        globalOptions.setVector(name, dataType, numDims, val);
    }

    public boolean isGlobalOptionSet(String name) {
        return globalOptions.isOptionSet(name);
    }

    public void setAttributeOptions(AttKeyT attKey, Options options) {
        attributeOptions.put(attKey, options);
    }

    public Options findAttributeOptions(AttKeyT attKey) {
        return attributeOptions.get(attKey);
    }

    private Options getAttributeOptions(AttKeyT attKey) {
        return attributeOptions.computeIfAbsent(attKey, k -> new Options());
    }

    private Options attributeOrGlobalOptions(AttKeyT attKey, String name) {
        Options attOptions = this.findAttributeOptions(attKey);
        if (attOptions != null && attOptions.isOptionSet(name)) {
            return attOptions;
        }
        return globalOptions;
    }

}
