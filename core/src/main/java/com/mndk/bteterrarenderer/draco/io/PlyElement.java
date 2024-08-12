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
