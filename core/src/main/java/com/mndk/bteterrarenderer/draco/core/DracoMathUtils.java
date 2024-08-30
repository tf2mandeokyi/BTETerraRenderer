/*
 * Copyright (C) 2024 The Draco Authors (for providing the original C++ code)
 * Copyright (C) 2024 m4ndeokyi (for translating the code into Java)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
