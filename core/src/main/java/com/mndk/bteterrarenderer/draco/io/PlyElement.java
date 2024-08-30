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

package com.mndk.bteterrarenderer.draco.io;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class PlyElement {

    private final String name;
    private final long numEntries;
    private final List<PlyProperty> properties = new ArrayList<>();
    private final Map<String, Integer> propertyIndex = new HashMap<>();

    public void addProperty(PlyProperty prop) {
        propertyIndex.put(prop.getName(), properties.size());
        properties.add(prop);
        if (!prop.isList()) {
            prop.reserveData((int) numEntries);
        }
    }

    public PlyProperty getPropertyByName(String name) {
        if(!propertyIndex.containsKey(name)) return null;
        return properties.get(propertyIndex.get(name));
    }

    public int getNumProperties() {
        return properties.size();
    }

    public int getNumEntries() {
        return (int) numEntries;
    }

    public PlyProperty getProperty(int propIndex) {
        return properties.get(propIndex);
    }

}
