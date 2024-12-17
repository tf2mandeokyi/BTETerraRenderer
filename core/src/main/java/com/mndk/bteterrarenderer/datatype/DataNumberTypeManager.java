package com.mndk.bteterrarenderer.datatype;

import com.mndk.bteterrarenderer.core.util.BTRUtil;
import com.mndk.bteterrarenderer.datatype.number.UByte;
import com.mndk.bteterrarenderer.datatype.number.UInt;
import com.mndk.bteterrarenderer.datatype.number.ULong;
import com.mndk.bteterrarenderer.datatype.number.UShort;
import lombok.experimental.UtilityClass;

@UtilityClass
class DataNumberTypeManager {

    Endian ENDIAN = Endian.LITTLE;

    final DataNumberType<Boolean> BOOLEAN = new BooleanType((byte) 0);
    final DataNumberType<Byte> BYTE = new ByteType((byte) 1);
    final DataNumberType<UByte> UBYTE = new UByteType((byte) 2);
    final DataNumberType<Short> SHORT = new ShortType((byte) 3);
    final DataNumberType<UShort> USHORT = new UShortType((byte) 4);
    final DataNumberType<Integer> INT = new IntType((byte) 5);
    final DataNumberType<UInt> UINT = new UIntType((byte) 6);
    final DataNumberType<Long> LONG = new LongType((byte) 7);
    final DataNumberType<ULong> ULONG = new ULongType((byte) 8);
    final DataNumberType<Float> FLOAT = new FloatType((byte) 9);
    final DataNumberType<Double> DOUBLE = new DoubleType((byte) 10);

    private final byte ID_COUNT = 11;
    private final DataNumberType<?>[] ID_TO_TYPE = new DataNumberType<?>[ID_COUNT];

    // To make the arithmetic operator comparison work faster, we pre-calculate the results
    // of the arithmetic operator comparison and store them in a matrix.
    // Note: outer array = left operand, inner array = right operand
    private final byte[][] MAPPING_MATRIX = new byte[ID_COUNT][ID_COUNT];

    /**
     * Returns the result of the arithmetic operator comparison between two data number types.
     * @param left The left operand
     * @param right The right operand
     * @return The result of the arithmetic operator comparison
     * @param <T> The result type
     */
    <T> DataNumberType<T> biOp(DataNumberType<?> left, DataNumberType<?> right) {
        return BTRUtil.uncheckedCast(ID_TO_TYPE[MAPPING_MATRIX[left.getId()][right.getId()]]);
    }

    private DataNumberType<?> convertArithmeticOperator(DataNumberType<?> left, DataNumberType<?> right) {
        // First, if the corresponding real type of either operand is double,
        // the other operand is converted, without change of type domain,
        // to a type whose corresponding real type is double.
        if (left.getId() == DOUBLE.getId() || right.getId() == DOUBLE.getId()) return DataType.float64();

        // Otherwise, if the corresponding real type of either operand is float,
        // the other operand is converted, without change of type domain,
        // to a type whose corresponding real type is float.
        if (left.getId() == FLOAT.getId() || right.getId() == FLOAT.getId()) return DataType.float32();

        // Otherwise, the integer promotions are performed on both operands.
        // Then the following rules are applied to the promoted operands:
        //
        // 1. If both operands have the same type, then no further conversion is needed.
        if (left.equals(right)) return left;

        // 2. Otherwise, if one of the operands is boolean, the boolean type
        // gets converted to the other type.
        if (left.getId() == BOOLEAN.getId()) return right;
        if (right.getId() == BOOLEAN.getId()) return left;

        // 3. Otherwise, if both operands have signed integer types or both have
        // unsigned integer types, the operand with the type of lesser integer conversion rank
        // is converted to the type of the operand with greater rank.
        if (left.isSigned() == right.isSigned()) return left.byteSize() > right.byteSize() ? left : right;

        // 4. Otherwise, if the operand that has unsigned integer type has rank
        // greater or equal to the rank of the type of the other operand, then
        // the operand with signed integer type is converted to the type of the operand
        // with unsigned integer type.
        if (left.isUnsigned() && left.byteSize() >= right.byteSize()) return left;
        if (right.isUnsigned() && right.byteSize() >= left.byteSize()) return right;

        // 5. Otherwise, if the type of the operand with signed integer type can
        // represent all of the values of the type of the operand with unsigned integer type,
        // then the operand with unsigned integer type is converted to the type
        // of the operand with signed integer type.
        if (left.isSigned() && left.byteSize() > right.byteSize()) return left;
        if (right.isSigned() && right.byteSize() > left.byteSize()) return right;

        // 6. Otherwise, both operands are converted to the unsigned integer type
        // corresponding to the type of the operand with signed integer type.
        return convertArithmeticOperator(left.getUnsigned(), right.getUnsigned());
    }

    static {
        ID_TO_TYPE[BOOLEAN.getId()] = DataType.bool();
        ID_TO_TYPE[BYTE.getId()] = DataType.int8();
        ID_TO_TYPE[UBYTE.getId()] = DataType.uint8();
        ID_TO_TYPE[SHORT.getId()] = DataType.int16();
        ID_TO_TYPE[USHORT.getId()] = DataType.uint16();
        ID_TO_TYPE[INT.getId()] = DataType.int32();
        ID_TO_TYPE[UINT.getId()] = DataType.uint32();
        ID_TO_TYPE[LONG.getId()] = DataType.int64();
        ID_TO_TYPE[ULONG.getId()] = DataType.uint64();
        ID_TO_TYPE[FLOAT.getId()] = DataType.float32();
        ID_TO_TYPE[DOUBLE.getId()] = DataType.float64();

        for (byte i = 0; i < ID_COUNT; ++i) {
            for (byte j = 0; j < ID_COUNT; ++j) {
                MAPPING_MATRIX[i][j] = convertArithmeticOperator(ID_TO_TYPE[i], ID_TO_TYPE[j]).getId();
            }
        }
    }

}
