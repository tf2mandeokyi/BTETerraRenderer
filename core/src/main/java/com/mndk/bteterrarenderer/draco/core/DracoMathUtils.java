package com.mndk.bteterrarenderer.draco.core;

import com.mndk.bteterrarenderer.datatype.number.ULong;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DracoMathUtils {

    public ULong intSqrt(ULong number) {
        if (number.equals(0)) return ULong.ZERO;
        ULong actNumber = number;
        ULong squareRoot = ULong.of(1);
        while (actNumber.ge(2)) {
            squareRoot = squareRoot.mul(2);
            actNumber = actNumber.div(4);
        }
        do { squareRoot = squareRoot.add(number.div(squareRoot)).div(2); }
        while (squareRoot.mul(squareRoot).gt(number));
        return squareRoot;
    }

}
