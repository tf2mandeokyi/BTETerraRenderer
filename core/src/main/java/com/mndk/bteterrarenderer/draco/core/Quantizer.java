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

/**
 * Class for quantizing single precision floating point values. The values
 * should be centered around zero and be within interval ({@code -range}, {@code +range}), where
 * the range is specified in the {@link Quantizer#init} method. Alternatively, the quantization
 * can be defined by {@code delta} that specifies the distance between two quantized values. Note
 * that the quantizer always snaps the values to the nearest integer value. E.g. for {@code delta
 * == 1.f}, values {@code -0.4f} and {@code 0.4f} would be both quantized to 0 while value {@code 0.6f} would be quantized
 * to 1. If a value lies exactly between two quantized states, it is always rounded up. E.g.,
 * for {@code delta == 1.f}, value {@code -0.5f} would be quantized to 0 while {@code 0.5f} would be
 * quantized to 1.
 */
public class Quantizer {

    private float inverseDelta = 1.0f;

    public void init(float range, int maxQuantizedValue) {
        this.inverseDelta = (float) maxQuantizedValue / range;
    }

    public void init(float delta) {
        this.inverseDelta = 1.0f / delta;
    }

    public int quantizeFloat(float val) {
        val *= inverseDelta;
        return (int) Math.floor(val + 0.5f);
    }
}
