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

package com.mndk.bteterrarenderer.draco.compression.attributes;

import com.mndk.bteterrarenderer.datatype.DataNumberType;
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.draco.core.Status;
import lombok.Getter;

public class OctahedronToolBox {

    @Getter private int quantizationBits = -1;
    @Getter private int maxQuantizedValue = -1;
    @Getter private int maxValue = -1;
    private float dequantizationScale = 1.0f;
    @Getter private int centerValue = -1;

    public Status setQuantizationBits(int q) {
        if (q < 2 || q > 30) {
            return Status.invalidParameter("Quantization bits must be between 2 and 30");
        }
        this.quantizationBits = q;
        this.maxQuantizedValue = (1 << this.quantizationBits) - 1;
        this.maxValue = this.maxQuantizedValue - 1;
        this.dequantizationScale = 2.0f / this.maxValue;
        this.centerValue = this.maxValue / 2;
        return Status.ok();
    }

    public boolean isInitialized() {
        return this.quantizationBits != -1;
    }

    /**
     * Convert all edge points in the top left and bottom right quadrants to
     * their corresponding position in the bottom left and top right quadrants.
     * Convert all corner edge points to the top right corner.
     */
    public void canonicalizeOctahedralCoords(int s, int t, Pointer<Integer> outS, Pointer<Integer> outT) {
        if ((s == 0 && t == 0) || (s == 0 && t == this.maxValue) || (s == this.maxValue && t == 0)) {
            s = this.maxValue;
            t = this.maxValue;
        } else if (s == 0 && t > this.centerValue) {
            t = this.centerValue - (t - this.centerValue);
        } else if (s == this.maxValue && t < this.centerValue) {
            t = this.centerValue + (this.centerValue - t);
        } else if (t == this.maxValue && s < this.centerValue) {
            s = this.centerValue + (this.centerValue - s);
        } else if (t == 0 && s > this.centerValue) {
            s = this.centerValue - (s - this.centerValue);
        }
        outS.set(s);
        outT.set(t);
    }

    public void integerVectorToQuantizedOctahedralCoords(Pointer<Integer> intVec,
                                                         Pointer<Integer> outS, Pointer<Integer> outT) {
        int vec0 = intVec.get(0), vec1 = intVec.get(1), vec2 = intVec.get(2);
        if (Math.abs(vec0) + Math.abs(vec1) + Math.abs(vec2) != this.centerValue) {
            throw new IllegalArgumentException("The absolute sum of the integer vector must equal the center value");
        }
        int s, t;
        if (vec0 >= 0) {
            // Right hemisphere.
            s = (vec1 + this.centerValue);
            t = (vec2 + this.centerValue);
        } else {
            // Left hemisphere.
            if (vec1 < 0) {
                s = Math.abs(vec2);
            } else {
                s = (this.maxValue - Math.abs(vec2));
            }
            if (vec2 < 0) {
                t = Math.abs(vec1);
            } else {
                t = (this.maxValue - Math.abs(vec1));
            }
        }
        canonicalizeOctahedralCoords(s, t, outS, outT);
    }

    public <T>
    void floatVectorToQuantizedOctahedralCoords(Pointer<T> vector,
                                                Pointer<Integer> outS, Pointer<Integer> outT) {
        DataNumberType<T> type = vector.getType().asNumber();
        double absSum = Math.abs(type.toDouble(vector.get(0))) +
                        Math.abs(type.toDouble(vector.get(1))) +
                        Math.abs(type.toDouble(vector.get(2)));
        // Adjust values such that abs sum equals 1.
        double[] scaledVector = new double[3];
        if (absSum > 1e-6) {
            // Scale needed to project the vector to the surface of an octahedron.
            double scale = 1.0 / absSum;
            scaledVector[0] = type.toDouble(vector.get(0)) * scale;
            scaledVector[1] = type.toDouble(vector.get(1)) * scale;
            scaledVector[2] = type.toDouble(vector.get(2)) * scale;
        } else {
            scaledVector[0] = 1.0;
            scaledVector[1] = 0;
            scaledVector[2] = 0;
        }

        // Scale vector such that the sum equals the center value.
        int[] intVec = new int[3];
        intVec[0] = (int) Math.floor(scaledVector[0] * this.centerValue + 0.5);
        intVec[1] = (int) Math.floor(scaledVector[1] * this.centerValue + 0.5);
        // Make sure the sum is exactly the center value.
        intVec[2] = this.centerValue - Math.abs(intVec[0]) - Math.abs(intVec[1]);
        if (intVec[2] < 0) {
            // If the sum of first two coordinates is too large, we need to decrease
            // the length of one of the coordinates.
            if (intVec[1] > 0) {
                intVec[1] += intVec[2];
            } else {
                intVec[1] -= intVec[2];
            }
            intVec[2] = 0;
        }
        // Take care of the sign.
        if (scaledVector[2] < 0) {
            intVec[2] *= -1;
        }

        integerVectorToQuantizedOctahedralCoords(Pointer.wrap(intVec), outS, outT);
    }

    public <T> void canonicalizeIntegerVector(Pointer<T> vec) {
        DataNumberType<T> inType = vec.getType().asNumber();
        if (!inType.isIntegral()) throw new IllegalArgumentException("T must be an integral type");
        if (!inType.isSigned()) throw new IllegalArgumentException("T must be a signed type");

        T vec0 = vec.get(0);
        T vec1 = vec.get(1);
        T vec2 = vec.get(2);
        long absSum = inType.toLong(inType.abs(vec0)) + inType.toLong(inType.abs(vec1)) + inType.toLong(inType.abs(vec2));

        if (absSum == 0) {
            vec0 = inType.from(this.centerValue);
        } else {
            vec0 = inType.from((inType.toLong(vec0) * (long) this.centerValue) / absSum);
            vec1 = inType.from((inType.toLong(vec1) * (long) this.centerValue) / absSum);
            if (inType.ge(vec2, 0)) {
                vec2 = inType.sub(inType.sub(this.centerValue, inType.abs(vec0)), inType.abs(vec1));
            } else {
                vec2 = inType.negate(inType.sub(inType.sub(this.centerValue, inType.abs(vec0)), inType.abs(vec1)));
            }
        }
        vec.set(0, vec0);
        vec.set(1, vec1);
        vec.set(2, vec2);
    }

    public void quantizedOctahedralCoordsToUnitVector(int inS, int inT, Pointer<Float> out) {
        octahedralCoordsToUnitVector(inS * this.dequantizationScale - 1.0f,
                inT * this.dequantizationScale - 1.0f, out);
    }

    public boolean isInDiamond(int s, int t) {
        if (s > this.centerValue || t > this.centerValue || s < -this.centerValue || t < -this.centerValue) {
            return false;
        }
        int st = Math.abs(s) + Math.abs(t);
        return st <= this.centerValue;
    }

    public void invertDiamond(Pointer<Integer> s, Pointer<Integer> t) {
        int sout = s.get();
        int tout = t.get();
        if (sout > this.centerValue) throw new IllegalArgumentException("s must be <= the center value");
        if (tout > this.centerValue) throw new IllegalArgumentException("t must be <= the center value");
        if (sout < -this.centerValue) throw new IllegalArgumentException("s must be >= the negative center value");
        if (tout < -this.centerValue) throw new IllegalArgumentException("t must be >= the negative center value");
        int signS, signT;
        if (sout >= 0 && tout >= 0) {
            signS = 1;
            signT = 1;
        } else if (sout <= 0 && tout <= 0) {
            signS = -1;
            signT = -1;
        } else {
            signS = (sout > 0) ? 1 : -1;
            signT = (tout > 0) ? 1 : -1;
        }

        // Perform the addition and subtraction using unsigned integers to avoid
        // signed integer overflows for bad data. Note that the result will be
        // unchanged for non-overflowing cases.
        int cornerPointS = signS * this.centerValue;
        int cornerPointT = signT * this.centerValue;
        int us = sout;
        int ut = tout;
        us = us + us - cornerPointS;
        ut = ut + ut - cornerPointT;
        if (signS * signT >= 0) {
            int temp = us;
            us = -ut;
            ut = -temp;
        } else {
            int temp = us;
            us = ut;
            ut = temp;
        }
        us = us + cornerPointS;
        ut = ut + cornerPointT;

        us /= 2;
        ut /= 2;
        s.set(us);
        t.set(ut);
    }

    public void invertDirection(Pointer<Integer> s, Pointer<Integer> t) {
        // Expect center already at origin.
        if (s.get() > this.centerValue) throw new IllegalArgumentException("s must be less than or equal to the center value");
        if (t.get() > this.centerValue) throw new IllegalArgumentException("t must be less than or equal to the center value");
        if (s.get() < -this.centerValue) throw new IllegalArgumentException("s must be greater than or equal to the negative center value");
        if (t.get() < -this.centerValue) throw new IllegalArgumentException("t must be greater than or equal to the negative center value");
        s.set(-s.get());
        t.set(-t.get());
        this.invertDiamond(s, t);
    }

    /** For correction values. */
    public int modMax(int x) {
        if (x > this.centerValue) return x - this.maxQuantizedValue;
        if (x < -this.centerValue) return x + this.maxQuantizedValue;
        return x;
    }

    /** For correction values. */
    public int makePositive(int x) {
        if (x < 0) return x + this.maxQuantizedValue;
        return x;
    }

    private void octahedralCoordsToUnitVector(float inS, float inT, Pointer<Float> out) {
        // Background about the encoding:
        //   A normal is encoded in a normalized space <s, t> depicted below. The
        //   encoding correponds to an octahedron that is unwrapped to a 2D plane.
        //   During encoding, a normal is projected to the surface of the octahedron
        //   and the projection is then unwrapped to the 2D plane. Decoding is the
        //   reverse of this process.
        //   All points in the central diamond are located on triangles on the
        //   right "hemisphere" of the octahedron while all points outside the
        //   diamond are on the left hemisphere (basically, they would have to be
        //   wrapped along the diagonal edges to form the octahedron). The central
        //   point corresponds to the right most vertex of the octahedron and all
        //   corners of the plane correspond to the left most vertex of the
        //   octahedron.
        //
        // t
        // ^ *-----*-----*
        // | |    /|\    |
        //   |   / | \   |
        //   |  /  |  \  |
        //   | /   |   \ |
        //   *-----*---- *
        //   | \   |   / |
        //   |  \  |  /  |
        //   |   \ | /   |
        //   |    \|/    |
        //   *-----*-----*  --> s

        // Note that the input |inS| and |inT| are already scaled to
        // <-1, 1> range. This way, the central point is at coordinate (0, 0).
        float y = inS;
        float z = inT;

        // Remaining coordinate can be computed by projecting the (y, z) values onto
        // the surface of the octahedron.
        float x = 1.0f - Math.abs(y) - Math.abs(z);

        // x is essentially a signed distance from the diagonal edges of the
        // diamond shown on the figure above. It is positive for all points in the
        // diamond (right hemisphere) and negative for all points outside the
        // diamond (left hemisphere). For all points on the left hemisphere we need
        // to update their (y, z) coordinates to account for the wrapping along
        // the edges of the diamond.
        float xOffset = -x;
        xOffset = xOffset < 0 ? 0 : xOffset;

        // This will do nothing for the points on the right hemisphere but it will
        // mirror the (y, z) location along the nearest diagonal edge of the
        // diamond.
        y += y < 0 ? xOffset : -xOffset;
        z += z < 0 ? xOffset : -xOffset;

        // Normalize the computed vector.
        float normSquared = x * x + y * y + z * z;
        if (normSquared < 1e-6) {
            out.set(0, 0f);
            out.set(1, 0f);
            out.set(2, 0f);
        } else {
            float d = 1.0f / (float) Math.sqrt(normSquared);
            out.set(0, x * d);
            out.set(1, y * d);
            out.set(2, z * d);
        }
    }
}
