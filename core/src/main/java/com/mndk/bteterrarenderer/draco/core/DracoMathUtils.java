package com.mndk.bteterrarenderer.draco.core;

import lombok.experimental.UtilityClass;

@UtilityClass
public class DracoMathUtils {

    public long intSqrt(long number) {
        if (number == 0) return 0;
        long actNumber = number;
        long squareRoot = 1;
        while (actNumber >= 2) {
            squareRoot *= 2;
            actNumber /= 4;
        }
        do { squareRoot = (squareRoot + number / squareRoot) / 2; }
        while (squareRoot * squareRoot > number);
        return squareRoot;
    }

}
