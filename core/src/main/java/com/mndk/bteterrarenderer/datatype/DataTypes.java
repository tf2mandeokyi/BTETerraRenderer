package com.mndk.bteterrarenderer.datatype;

import com.mndk.bteterrarenderer.datatype.number.*;
import com.mndk.bteterrarenderer.datatype.number.type.*;
import lombok.experimental.UtilityClass;

@UtilityClass
class DataTypes {
    final DataType<?, Object[]> DEFAULT = new ObjectType<>();
    final DataNumberType<Boolean, boolean[]> BOOLEAN = new BooleanType();
    final DataNumberType<Byte, byte[]> BYTE = new ByteType();
    final DataNumberType<UByte, byte[]> UBYTE = new UByteType();
    final DataNumberType<Short, short[]> SHORT = new ShortType();
    final DataNumberType<UShort, short[]> USHORT = new UShortType();
    final DataNumberType<Integer, int[]> INT = new IntType();
    final DataNumberType<UInt, int[]> UINT = new UIntType();
    final DataNumberType<Long, long[]> LONG = new LongType();
    final DataNumberType<ULong, long[]> ULONG = new ULongType();
    final DataNumberType<Float, float[]> FLOAT = new FloatType();
    final DataNumberType<Double, double[]> DOUBLE = new DoubleType();
}
