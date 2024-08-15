package com.mndk.bteterrarenderer.draco.core;

import com.mndk.bteterrarenderer.core.util.BTRUtil;
import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.DataNumberType;
import com.mndk.bteterrarenderer.datatype.number.UByte;
import lombok.Getter;

import javax.annotation.Nullable;

@Getter
public enum DracoDataType {
    INVALID(0, null),
    INT8(1, DataType.int8()),
    UINT8(2, DataType.uint8()),
    INT16(3, DataType.int16()),
    UINT16(4, DataType.uint16()),
    INT32(5, DataType.int32()),
    UINT32(6, DataType.uint32()),
    INT64(7, DataType.int64()),
    UINT64(8, DataType.uint64()),
    FLOAT32(9, DataType.float32()),
    FLOAT64(10, DataType.float64()),
    BOOL(11, DataType.bool());

    public static final int COUNT = values().length;

    private final UByte id;
    private final DataNumberType<?> actualType;

    DracoDataType(int id, @Nullable DataNumberType<?> actualType) {
        this.id = UByte.of(id);
        this.actualType = actualType;
    }

    public long getDataTypeLength() {
        return actualType == null ? -1 : actualType.byteSize();
    }
    public boolean isDataTypeIntegral() {
        return actualType != null && actualType.isIntegral();
    }
    public <T> DataNumberType<T> getActualType() {
        return BTRUtil.uncheckedCast(actualType);
    }

    public static DracoDataType valueOf(UByte value) {
        for(DracoDataType type : values()) {
            if(type.id.equals(value)) return type;
        }
        return INVALID;
    }
}
