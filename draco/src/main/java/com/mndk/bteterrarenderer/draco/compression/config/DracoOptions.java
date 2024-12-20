/*
 * Copyright (C) 2024 The Draco Authors (for providing the original C++ code)
 * Copyright (C) 2024 m4ndeokyi (for translating the code into Java)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mndk.bteterrarenderer.draco.compression.config;

import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.draco.core.Options;
import com.mndk.bteterrarenderer.draco.core.VectorD;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@SuppressWarnings("unused")
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

    public <S, V extends VectorD<S, V>> V getAttributeVector(AttKeyT attKey, String name, V outVec) {
        return this.attributeOrGlobalOptions(attKey, name).getVector(name, outVec);
    }

    public <S, V extends VectorD<S, V>> void setAttributeVector(AttKeyT attKey, String name, V val) {
        this.getAttributeOptions(attKey).setVector(name, val);
    }

    public <T> boolean getAttributeVector(AttKeyT attKey, String name, int numDims, Pointer<T> array) {
        return this.attributeOrGlobalOptions(attKey, name).getVector(name, numDims, array);
    }

    public <T> void setAttributeVector(AttKeyT attKey, String name, int numDims, Pointer<T> array) {
        this.getAttributeOptions(attKey).setVector(name, numDims, array);
    }

    public boolean isAttributeOptionSet(AttKeyT attKey, String name) {
        return this.attributeOrGlobalOptions(attKey, name).isOptionSet(name);
    }

    public int getGlobalInt(String name, int defaultVal) {
        return globalOptions.getInt(name, defaultVal);
    }

    public <T extends Enum<T>> T getGlobalEnum(String name, Function<Integer, T> function, T defaultVal) {
        return globalOptions.getEnum(name, function, defaultVal);
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

    public <S, V extends VectorD<S, V>> V getGlobalVector(String name, V outVec) {
        return globalOptions.getVector(name, outVec);
    }

    public <S, V extends VectorD<S, V>> void setGlobalVector(String name, V val) {
        globalOptions.setVector(name, val);
    }

    public <T> boolean getGlobalVector(String name, Pointer<T> vec, int numDims) {
        return globalOptions.getVector(name, numDims, vec);
    }

    public <T> void setGlobalVector(String name, Pointer<T> vec, int numDims) {
        globalOptions.setVector(name, numDims, vec);
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
