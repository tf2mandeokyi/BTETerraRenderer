package com.mndk.bteterrarenderer.draco.io;

import com.mndk.bteterrarenderer.datatype.number.DataNumberType;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PlyPropertyReader<T> {

    private final DataNumberType<T> type;
    private final PlyProperty property;

    public <U> T readValue(int valueId) {
        DataNumberType<U> propertyType = property.getDataType().getActualType();
        U value = property.getDataEntryAddress(valueId, propertyType).get();
        return type.from(propertyType, value);
    }

}
