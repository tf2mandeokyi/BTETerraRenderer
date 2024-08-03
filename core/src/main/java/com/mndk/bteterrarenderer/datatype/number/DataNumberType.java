package com.mndk.bteterrarenderer.datatype.number;

import com.mndk.bteterrarenderer.core.util.BTRUtil;
import com.mndk.bteterrarenderer.datatype.DataType;

public interface DataNumberType<T> extends DataType<T>, DataCalculator<T> {
    // Type conversions
    DataNumberType<?> getSigned();
    DataNumberType<?> getUnsigned();
    default <U> DataNumberType<U> makeSigned() { return BTRUtil.uncheckedCast(this.getSigned()); }
    default <U> DataNumberType<U> makeUnsigned() { return BTRUtil.uncheckedCast(this.getUnsigned()); }
}
