//// A single PLY element such as "vertex" or "face". Each element can store
//// arbitrary properties such as vertex coordinates or face indices.
//class PlyElement {
// public:
//  PlyElement(const std::string &name, int64_t num_entries);
//  void AddProperty(const PlyProperty &prop) {
//    property_index_[prop.name()] = static_cast<int>(properties_.size());
//    properties_.emplace_back(prop);
//    if (!properties_.back().is_list()) {
//      properties_.back().ReserveData(static_cast<int>(num_entries_));
//    }
//  }
//
//  const PlyProperty *GetPropertyByName(const std::string &name) const {
//    const auto it = property_index_.find(name);
//    if (it != property_index_.end()) {
//      return &properties_[it->second];
//    }
//    return nullptr;
//  }
//
//  int num_properties() const { return static_cast<int>(properties_.size()); }
//  int num_entries() const { return static_cast<int>(num_entries_); }
//  const PlyProperty &property(int prop_index) const {
//    return properties_[prop_index];
//  }
//  PlyProperty &property(int prop_index) { return properties_[prop_index]; }
//
// private:
//  std::string name_;
//  int64_t num_entries_;
//  std::vector<PlyProperty> properties_;
//  std::map<std::string, int> property_index_;
//};

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
