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

import org.junit.Assert;
import org.junit.Test;

public class QuantizationTest {

    @Test
    public void testQuantizer() {
        Quantizer quantizer = new Quantizer();
        quantizer.init(10.0f, 255);
        Assert.assertEquals(quantizer.quantizeFloat(0.0f), 0);
        Assert.assertEquals(quantizer.quantizeFloat(10.0f), 255);
        Assert.assertEquals(quantizer.quantizeFloat(-10.0f), -255);
        Assert.assertEquals(quantizer.quantizeFloat(4.999f), 127);
        Assert.assertEquals(quantizer.quantizeFloat(5.0f), 128);
        Assert.assertEquals(quantizer.quantizeFloat(-4.9999f), -127);
        // Note: Both -5.f and +5.f lie exactly on the boundary between two
        // quantized values (127.5f and -127.5f). Due to rounding, both values are
        // then converted to 128 and -127 respectively.
        Assert.assertEquals(quantizer.quantizeFloat(-5.0f), -127);
        Assert.assertEquals(quantizer.quantizeFloat(-5.0001f), -128);

        // Out of range quantization.
        // The behavior is technically undefined, but both quantizer and dequantizer
        // should still work correctly unless the quantized values overflow.
        Assert.assertTrue(quantizer.quantizeFloat(-15.0f) < -255);
        Assert.assertTrue(quantizer.quantizeFloat(15.0f) > 255);
    }

    @Test
    public void testDequantizer() {
        Dequantizer dequantizer = new Dequantizer();
        StatusAssert.assertOk(dequantizer.init(10.0f, 255));
        Assert.assertEquals(dequantizer.dequantizeFloat(0), 0.0f, 0.000000001);
        Assert.assertEquals(dequantizer.dequantizeFloat(255), 10.0f, 0.000000001);
        Assert.assertEquals(dequantizer.dequantizeFloat(-255), -10.0f, 0.000000001);
        Assert.assertEquals(dequantizer.dequantizeFloat(128), 10.0f * (128.0f / 255.0f), 0.000000001);

        // Test that the dequantizer fails to initialize with invalid input
        // parameters.
        StatusAssert.assertError(dequantizer.init(1.0f, 0));
        StatusAssert.assertError(dequantizer.init(1.0f, -4));
    }

}
