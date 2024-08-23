package com.mndk.bteterrarenderer.draco.io;

import com.mndk.bteterrarenderer.datatype.number.DataNumberType;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PlyPropertyWriter<T> {

    private final DataNumberType<T> type;
    private final PlyProperty property;

    public <U> void pushBackValue(T value) {
        DataNumberType<U> propertyType = property.getDataType().getActualType();
        U casted = propertyType.from(this.type, value);
        property.pushBackValue(propertyType.newOwned(casted));
    }

}
