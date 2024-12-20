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

package com.mndk.bteterrarenderer.draco.compression.attributes.scheme;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.draco.core.VectorD;
import org.junit.Assert;
import org.junit.Test;

public class PSchemeNormalOctahedronCanonicalizedTransformTest {

    private void testComputeCorrection(PSchemeNormalOctahedronCanonicalizedEncodingTransform<Integer> transform,
                                       int ox, int oy, int px, int py, int cx, int cy) {
        int[] o = { ox + 7, oy + 7 };
        int[] p = { px + 7, py + 7 };
        int[] corr = { 500, 500 };
        transform.computeCorrection(Pointer.wrap(o), Pointer.wrap(p), Pointer.wrap(corr));
        Assert.assertEquals(corr[0], (cx + 15) % 15);
        Assert.assertEquals(corr[1], (cy + 15) % 15);
    }

    private void testGetRotationCount(PSchemeNormalOctahedronCanonicalizedEncodingTransform<Integer> transform,
                                      VectorD.D2<Integer> pred, int rotDir) {
        int rotationCount = transform.getRotationCount(pred);
        Assert.assertEquals(rotDir, rotationCount);
    }

    private void testRotateRepresentation(PSchemeNormalOctahedronCanonicalizedEncodingTransform<Integer> transform,
                                          VectorD.D2<Integer> org, VectorD.D2<Integer> pred,
                                          VectorD.D2<Integer> rotOrg, VectorD.D2<Integer> rotPred) {
        int rotationCount = transform.getRotationCount(pred);
        VectorD.D2<Integer> resOrg = transform.rotatePoint(org, rotationCount);
        VectorD.D2<Integer> resPred = transform.rotatePoint(pred, rotationCount);
        Assert.assertEquals(rotOrg.get(0), resOrg.get(0));
        Assert.assertEquals(rotOrg.get(1), resOrg.get(1));
        Assert.assertEquals(rotPred.get(0), resPred.get(0));
        Assert.assertEquals(rotPred.get(1), resPred.get(1));
    }

    @Test
    public void init() {
        PSchemeNormalOctahedronCanonicalizedEncodingTransform<Integer> transform =
                new PSchemeNormalOctahedronCanonicalizedEncodingTransform<>(DataType.int32(), 15);
        Assert.assertTrue(transform.areCorrectionsPositive());
    }

    @Test
    public void isInBottomLeft() {
        PSchemeNormalOctahedronCanonicalizedEncodingTransform<Integer> transform =
                new PSchemeNormalOctahedronCanonicalizedEncodingTransform<>(DataType.int32(), 15);
        Assert.assertTrue(transform.isInBottomLeft(VectorD.int2(0, 0)));
        Assert.assertTrue(transform.isInBottomLeft(VectorD.int2(-1, -1)));
        Assert.assertTrue(transform.isInBottomLeft(VectorD.int2(-7, -7)));

        Assert.assertFalse(transform.isInBottomLeft(VectorD.int2(1, 1)));
        Assert.assertFalse(transform.isInBottomLeft(VectorD.int2(7, 7)));
        Assert.assertFalse(transform.isInBottomLeft(VectorD.int2(-1, 1)));
        Assert.assertFalse(transform.isInBottomLeft(VectorD.int2(-7, 7)));
        Assert.assertFalse(transform.isInBottomLeft(VectorD.int2(1, -1)));
        Assert.assertFalse(transform.isInBottomLeft(VectorD.int2(7, -7)));
    }

    @Test
    public void getRotationCount() {
        PSchemeNormalOctahedronCanonicalizedEncodingTransform<Integer> transform =
                new PSchemeNormalOctahedronCanonicalizedEncodingTransform<>(DataType.int32(), 15);
        testGetRotationCount(transform, VectorD.int2(1, 2), 2);
        testGetRotationCount(transform, VectorD.int2(-1, 2), 3);
        testGetRotationCount(transform, VectorD.int2(1, -2), 1);
        testGetRotationCount(transform, VectorD.int2(-1, -2), 0);
        testGetRotationCount(transform, VectorD.int2(0, 2), 3);
        testGetRotationCount(transform, VectorD.int2(0, -2), 1);
        testGetRotationCount(transform, VectorD.int2(2, 0), 2);
        testGetRotationCount(transform, VectorD.int2(-2, 0), 0);
        testGetRotationCount(transform, VectorD.int2(0, 0), 0);
    }

    @Test
    public void rotateRepresentation() {
        PSchemeNormalOctahedronCanonicalizedEncodingTransform<Integer> transform =
                new PSchemeNormalOctahedronCanonicalizedEncodingTransform<>(DataType.int32(), 15);
        // p top left; shift clockwise by 3
        testRotateRepresentation(transform, VectorD.int2(1, 2), VectorD.int2(-3, 1),
                VectorD.int2(-2, 1), VectorD.int2(-1, -3));
        testRotateRepresentation(transform, VectorD.int2(-1, -2), VectorD.int2(-3, 1),
                VectorD.int2(2, -1), VectorD.int2(-1, -3));
        testRotateRepresentation(transform, VectorD.int2(1, -2), VectorD.int2(-3, 1),
                VectorD.int2(2, 1), VectorD.int2(-1, -3));
        testRotateRepresentation(transform, VectorD.int2(-1, 2), VectorD.int2(-3, 1),
                VectorD.int2(-2, -1), VectorD.int2(-1, -3));
        // p top right; shift clockwise by 2 (flip)
        testRotateRepresentation(transform, VectorD.int2(1, 1), VectorD.int2(1, 3),
                VectorD.int2(-1, -1), VectorD.int2(-1, -3));
        testRotateRepresentation(transform, VectorD.int2(-1, -2), VectorD.int2(1, 3),
                VectorD.int2(1, 2), VectorD.int2(-1, -3));
        testRotateRepresentation(transform, VectorD.int2(-1, 2), VectorD.int2(1, 3),
                VectorD.int2(1, -2), VectorD.int2(-1, -3));
        testRotateRepresentation(transform, VectorD.int2(1, -2), VectorD.int2(1, 3),
                VectorD.int2(-1, 2), VectorD.int2(-1, -3));
        // p bottom right; shift clockwise by 1
        testRotateRepresentation(transform, VectorD.int2(1, 2), VectorD.int2(3, -1),
                VectorD.int2(2, -1), VectorD.int2(-1, -3));
        testRotateRepresentation(transform, VectorD.int2(1, -2), VectorD.int2(3, -1),
                VectorD.int2(-2, -1), VectorD.int2(-1, -3));
        testRotateRepresentation(transform, VectorD.int2(-1, -2), VectorD.int2(3, -1),
                VectorD.int2(-2, 1), VectorD.int2(-1, -3));
        testRotateRepresentation(transform, VectorD.int2(-1, 2), VectorD.int2(3, -1),
                VectorD.int2(2, 1), VectorD.int2(-1, -3));
        // p bottom left; no change
        testRotateRepresentation(transform, VectorD.int2(1, 2), VectorD.int2(-1, -3),
                VectorD.int2(1, 2), VectorD.int2(-1, -3));
        testRotateRepresentation(transform, VectorD.int2(-1, 2), VectorD.int2(-1, -3),
                VectorD.int2(-1, 2), VectorD.int2(-1, -3));
        testRotateRepresentation(transform, VectorD.int2(1, -2), VectorD.int2(-1, -3),
                VectorD.int2(1, -2), VectorD.int2(-1, -3));
        testRotateRepresentation(transform, VectorD.int2(-1, -2), VectorD.int2(-1, -3),
                VectorD.int2(-1, -2), VectorD.int2(-1, -3));
    }

    @Test
    public void computeCorrection() {
        PSchemeNormalOctahedronCanonicalizedEncodingTransform<Integer> transform =
                new PSchemeNormalOctahedronCanonicalizedEncodingTransform<>(DataType.int32(), 15);
        testComputeCorrection(transform, 0, 0, 0, 0, 0, 0);
        testComputeCorrection(transform, 1, 1, 1, 1, 0, 0);
        // inside diamond; p top right
        testComputeCorrection(transform, 3, 4, 1, 2, -2, -2);
        testComputeCorrection(transform, -3, 4, 1, 2, 4, -2);
        testComputeCorrection(transform, 3, -4, 1, 2, -2, 6);
        testComputeCorrection(transform, -3, -4, 1, 2, 4, 6);
        // inside diamond; p top left
        testComputeCorrection(transform, 3, 4, -1, 2, -2, 4);
        testComputeCorrection(transform, -3, 4, -1, 2, -2, -2);
        testComputeCorrection(transform, 3, -4, -1, 2, 6, 4);
        testComputeCorrection(transform, -3, -4, -1, 2, 6, -2);
        // inside diamond; p bottom right
        testComputeCorrection(transform, 3, 4, 1, -2, 6, -2);
        testComputeCorrection(transform, -3, 4, 1, -2, 6, 4);
        testComputeCorrection(transform, 3, -4, 1, -2, -2, -2);
        testComputeCorrection(transform, -3, -4, 1, -2, -2, 4);
        // inside diamond; p bottom left
        testComputeCorrection(transform, 3, 4, -1, -2, 4, 6);
        testComputeCorrection(transform, -3, 4, -1, -2, -2, 6);
        testComputeCorrection(transform, 3, -4, -1, -2, 4, -2);
        testComputeCorrection(transform, -3, -4, -1, -2, -2, -2);
        // outside diamond; p top right
        testComputeCorrection(transform, 1, 2, 5, 4, -2, -4);
        testComputeCorrection(transform, -1, 2, 5, 4, -7, -4);
        testComputeCorrection(transform, 1, -2, 5, 4, -2, -7);
        testComputeCorrection(transform, -1, -2, 5, 4, -7, -7);
        // outside diamond; p top left
        testComputeCorrection(transform, 1, 2, -5, 4, -4, -7);
        testComputeCorrection(transform, -1, 2, -5, 4, -4, -2);
        testComputeCorrection(transform, 1, -2, -5, 4, -7, -7);
        testComputeCorrection(transform, -1, -2, -5, 4, -7, -2);
        // outside diamond; p bottom right
        testComputeCorrection(transform, 1, 2, 5, -4, -7, -2);
        testComputeCorrection(transform, -1, 2, 5, -4, -7, -7);
        testComputeCorrection(transform, 1, -2, 5, -4, -4, -2);
        testComputeCorrection(transform, -1, -2, 5, -4, -4, -7);
        // outside diamond; p bottom left
        testComputeCorrection(transform, 1, 2, -5, -4, -7, -7);
        testComputeCorrection(transform, -1, 2, -5, -4, -2, -7);
        testComputeCorrection(transform, 1, -2, -5, -4, -7, -4);
        testComputeCorrection(transform, -1, -2, -5, -4, -2, -4);

        testComputeCorrection(transform, -1, -2, 7, 7, -5, -6);
        testComputeCorrection(transform, 0, 0, 7, 7, 7, 7);
        testComputeCorrection(transform, -1, -2, 0, -2, 0, 1);
    }

    @Test
    public void interfaceTest() {
        PSchemeNormalOctahedronCanonicalizedEncodingTransform<Integer> transform =
                new PSchemeNormalOctahedronCanonicalizedEncodingTransform<>(DataType.int32(), 15);
        Assert.assertEquals(15, transform.getMaxQuantizedValue().longValue());
        Assert.assertEquals(7, transform.getCenterValue().longValue());
        Assert.assertEquals(4, transform.getQuantizationBits());
    }

}
