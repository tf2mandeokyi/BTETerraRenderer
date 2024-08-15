package com.mndk.bteterrarenderer.datatype;

import com.mndk.bteterrarenderer.datatype.number.*;
import lombok.experimental.UtilityClass;

@UtilityClass
class DataTypeStorage {
    final DataNumberType<Boolean> BOOLEAN = new BooleanType();
    final DataNumberType<Byte> BYTE = new ByteType();
    final DataNumberType<UByte> UBYTE = new UByteType();
    final DataNumberType<Short> SHORT = new ShortType();
    final DataNumberType<UShort> USHORT = new UShortType();
    final DataNumberType<Integer> INT = new IntType();
    final DataNumberType<UInt> UINT = new UIntType();
    final DataNumberType<Long> LONG = new LongType();
    final DataNumberType<ULong> ULONG = new ULongType();
    final DataNumberType<Float> FLOAT = new FloatType();
    final DataNumberType<Double> DOUBLE = new DoubleType();

    Endian ENDIAN = Endian.LITTLE;
}
