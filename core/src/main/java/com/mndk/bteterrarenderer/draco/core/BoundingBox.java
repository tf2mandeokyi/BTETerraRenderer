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

import lombok.Getter;

@Getter
public class BoundingBox {

    private final VectorD.D3<Float> minPoint;
    private final VectorD.D3<Float> maxPoint;

    public BoundingBox() {
        this.minPoint = VectorD.float3(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
        this.maxPoint = VectorD.float3(Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE);
    }

    public BoundingBox(VectorD.D3<Float> minPoint, VectorD.D3<Float> maxPoint) {
        this.minPoint = minPoint;
        this.maxPoint = maxPoint;
    }

    public boolean isValid() {
        return minPoint.get(0) != Float.MAX_VALUE &&
               minPoint.get(1) != Float.MAX_VALUE &&
               minPoint.get(2) != Float.MAX_VALUE &&
               maxPoint.get(0) != Float.MIN_VALUE &&
               maxPoint.get(1) != Float.MIN_VALUE &&
               maxPoint.get(2) != Float.MIN_VALUE;
    }

    public void update(VectorD.D3<Float> newPoint) {
        for (int i = 0; i < 3; i++) {
            if (newPoint.get(i) < minPoint.get(i)) {
                minPoint.set(i, newPoint.get(i));
            }
            if (newPoint.get(i) > maxPoint.get(i)) {
                maxPoint.set(i, newPoint.get(i));
            }
        }
    }

    public void update(BoundingBox other) {
        update(other.minPoint);
        update(other.maxPoint);
    }

    public VectorD.D3<Float> size() {
        return maxPoint.subtract(minPoint);
    }

    public VectorD.D3<Float> center() {
        return minPoint.add(maxPoint).divide(2f);
    }
}
