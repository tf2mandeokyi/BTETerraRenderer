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

package com.mndk.bteterrarenderer.draco.compression.entropy;

import com.mndk.bteterrarenderer.datatype.number.UByte;
import com.mndk.bteterrarenderer.datatype.number.UInt;
import com.mndk.bteterrarenderer.datatype.pointer.RawPointer;
import com.mndk.bteterrarenderer.draco.core.Status;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Ans {
    public static final int DRACO_ANS_P8_PRECISION = 256;
    public static final int DRACO_ANS_L_BASE = 4096;
    public static final int DRACO_ANS_IO_BASE = 256;
    private static final double INV_LOG2 = 1.0 / Math.log(2);

    public UInt getLE16(RawPointer pointer) { return pointer.getRawUShort().uIntValue(); }
    public UInt getLE24(RawPointer pointer) { return pointer.getRawUInt24(); }
    public UInt getLE32(RawPointer pointer) { return pointer.getRawUInt(); }
    public void memPutLe16(RawPointer pointer, UInt val) { pointer.setRawShort(val.shortValue()); }
    public void memPutLe24(RawPointer pointer, UInt val) { pointer.setRawInt24(val); }
    public void memPutLe32(RawPointer pointer, UInt val) { pointer.setRawInt(val); }

    public double log2(double x) {
        return Math.log(x) * INV_LOG2;
    }

    public int computeRAnsUnclampedPrecision(int symbolsBitLength) {
        return (3 * symbolsBitLength) / 2;
    }

    public int computeRAnsPrecisionFromUniqueSymbolsBitLength(int symbolsBitLength) {
        return Math.min(Math.max(computeRAnsUnclampedPrecision(symbolsBitLength), 12), 20);
    }

    public long approximateRAnsFrequencyTableBits(int maxValue, int numUniqueSymbols) {
        long tableZeroFrequencyBits = 8L * (numUniqueSymbols + (maxValue - numUniqueSymbols) / 64);
        return 8L * numUniqueSymbols + tableZeroFrequencyBits;
    }

    public static class Coder {
        public RawPointer buf = null;
        public long bufOffset = 0;
        public UInt state = UInt.ZERO;

        public void ansWriteInit(RawPointer buf, UInt state) {
            this.buf = buf;
            this.state = state;
        }

        public void ansWriteInit(RawPointer buf) {
            this.ansWriteInit(buf, UInt.of(DRACO_ANS_L_BASE));
        }

        public long ansWriteEnd() {
            if (this.state.lt(DRACO_ANS_L_BASE) || this.state.ge(DRACO_ANS_L_BASE * DRACO_ANS_IO_BASE)) {
                throw new IllegalArgumentException("Illegal state number to be serialized");
            }
            UInt state = this.state.sub(DRACO_ANS_L_BASE);
            if (state.lt(1 << 6)) {
                this.buf.setRawByte(this.bufOffset, state.byteValue());
                return this.bufOffset + 1;
            } else if (state.lt(1 << 14)) {
                memPutLe16(this.buf.rawAdd(this.bufOffset), state.add(0x01 << 14));
                return this.bufOffset + 2;
            } else if (state.lt(1 << 22)) {
                memPutLe24(this.buf.rawAdd(this.bufOffset), state.add(0x02 << 22));
                return this.bufOffset + 3;
            } else {
                throw new IllegalArgumentException("State is too large to be serialized");
            }
        }

        public void rabsWrite(int val, UByte p0) {
            this.rabsDescWrite(val, p0);
        }

        /**
         * rABS with descending spread.
         * p or p0 takes the place of l_s from the paper.
         * DRACO_ANS_P8_PRECISION is m.
         */
        public void rabsDescWrite(int val, UByte p0) {
            UByte p = UByte.of(DRACO_ANS_P8_PRECISION).sub(p0);
            UInt l_s = val != 0 ? p.uIntValue() : p0.uIntValue();
            if (this.state.ge(UInt.of(DRACO_ANS_L_BASE / DRACO_ANS_P8_PRECISION * DRACO_ANS_IO_BASE).mul(l_s))) {
                this.buf.setRawByte(this.bufOffset++, this.state.mod(DRACO_ANS_IO_BASE).byteValue());
                this.state = this.state.div(DRACO_ANS_IO_BASE);
            }
            // DRACO_ANS_DIVREM(quot, rem, ans.getState(), l_s);
            UInt quot = this.state.div(l_s);
            UInt rem = this.state.sub(quot.mul(l_s));
            this.state = quot.mul(DRACO_ANS_P8_PRECISION).add(rem).add(val != 0 ? UInt.ZERO : p.uIntValue());
        }

        /**
         * rABS with ascending spread.
         * p or p0 takes the place of l_s from the paper.
         * DRACO_ANS_P8_PRECISION is m.
         */
        public void rabsAscWrite(int val, UByte p0) {
            UByte p = UByte.of(DRACO_ANS_P8_PRECISION).sub(p0);
            UInt l_s = val != 0 ? p.uIntValue() : p0.uIntValue();
            if (this.state.ge(UInt.of(DRACO_ANS_L_BASE / DRACO_ANS_P8_PRECISION * DRACO_ANS_IO_BASE).mul(l_s))) {
                this.buf.setRawByte(this.bufOffset++, this.state.mod(DRACO_ANS_IO_BASE).byteValue());
                this.state = this.state.div(DRACO_ANS_IO_BASE);
            }
            UInt quot = this.state.div(l_s);
            UInt rem = this.state.sub(quot.mul(l_s));
            this.state = quot.mul(DRACO_ANS_P8_PRECISION).add(rem).add(val != 0 ? p0.uIntValue() : UInt.ZERO);
        }

        public void uabsWrite(boolean val, UByte p0) {
            UByte p = UByte.of(DRACO_ANS_P8_PRECISION).sub(p0);
            UInt l_s = val ? p.uIntValue() : p0.uIntValue();
            while (this.state.ge(UInt.of(DRACO_ANS_L_BASE / DRACO_ANS_P8_PRECISION * DRACO_ANS_IO_BASE).mul(l_s))) {
                this.buf.setRawByte(this.bufOffset++, this.state.mod(DRACO_ANS_IO_BASE).byteValue());
                this.state = this.state.div(DRACO_ANS_IO_BASE);
            }
            if (!val) {
                this.state = this.state.mul(DRACO_ANS_P8_PRECISION).div(p0.uIntValue());
            } else {
                this.state = this.state.add(1).mul(DRACO_ANS_P8_PRECISION).add(p.uIntValue()).sub(1).div(p.uIntValue()).sub(1);
            }
        }
    }

    public static class Decoder {
        public RawPointer buf = null;
        public long bufOffset = 0;
        public UInt state = UInt.ZERO;

        public boolean rabsRead(UByte p0) {
            return this.rabsDescRead(p0);
        }

        public boolean rabsDescRead(UByte p0) {
            UByte p = UByte.of(DRACO_ANS_P8_PRECISION).sub(p0);
            if (this.state.lt(DRACO_ANS_L_BASE) && this.bufOffset > 0) {
                this.state = this.state.mul(DRACO_ANS_IO_BASE).add(this.buf.getRawUByte(--this.bufOffset).uIntValue());
            }
            UInt x = this.state;
            UInt quot = x.div(DRACO_ANS_P8_PRECISION);
            UInt rem = x.mod(DRACO_ANS_P8_PRECISION);
            UInt xn = quot.mul(p.uIntValue());
            boolean val = rem.lt(p.uIntValue());
            if (val) {
                this.state = xn.add(rem);
            } else {
                this.state = x.sub(xn).sub(p.uIntValue());
            }
            return val;
        }

        public boolean rabsAscRead(UByte p0) {
            UByte p = UByte.of(DRACO_ANS_P8_PRECISION).sub(p0);
            if (this.state.lt(DRACO_ANS_L_BASE)) {
                this.state = this.state.mul(DRACO_ANS_IO_BASE).add(this.buf.getRawUByte(--this.bufOffset).uIntValue());
            }
            UInt x = this.state;
            UInt quot = x.div(DRACO_ANS_P8_PRECISION);
            UInt rem = x.mod(DRACO_ANS_P8_PRECISION);
            UInt xn = quot.mul(p.uIntValue());
            boolean val = rem.ge(p0.uIntValue());
            if (val) {
                this.state = xn.add(rem).sub(p0.uIntValue());
            } else {
                this.state = x.sub(xn);
            }
            return val;
        }

        public boolean uabsRead(UByte p0) {
            UByte p = UByte.of(DRACO_ANS_P8_PRECISION).sub(p0);
            UInt state = this.state;
            while (state.lt(DRACO_ANS_L_BASE) && this.bufOffset > 0) {
                state = state.mul(DRACO_ANS_IO_BASE).add(this.buf.getRawUByte(--this.bufOffset).uIntValue());
            }
            UInt sp = state.mul(p.uIntValue());
            UInt xp = sp.div(DRACO_ANS_P8_PRECISION);
            boolean s = sp.and(0xFF).ge(p0.uIntValue());
            if (s) {
                this.state = xp;
            } else {
                this.state = state.sub(xp);
            }
            return s;
        }

        public int uabsReadBit() {
            UInt state = this.state;
            while (state.lt(DRACO_ANS_L_BASE) && this.bufOffset > 0) {
                state = state.mul(DRACO_ANS_IO_BASE).add(this.buf.getRawUByte(--this.bufOffset).uIntValue());
            }
            int s = state.and(1).intValue();
            this.state = state.shr(1);
            return s;
        }

        public Status ansReadInit(RawPointer buf, int offset) {
            if (offset < 1) {
                return Status.ioError("Buffer offset is too small: " + offset);
            }
            this.buf = buf;
            int x = buf.getRawUByte(offset - 1).shr(6).intValue();
            if (x == 0) {
                this.bufOffset = offset - 1;
                this.state = buf.getRawUByte(offset - 1).and(0x3F).uIntValue();
            } else if (x == 1) {
                if (offset < 2) {
                    return Status.ioError("Buffer offset is too small");
                }
                this.bufOffset = offset - 2;
                this.state = getLE16(buf.rawAdd(offset - 2)).and(0x3FFF).uIntValue();
            } else if (x == 2) {
                if (offset < 3) {
                    return Status.ioError("Buffer offset is too small");
                }
                this.bufOffset = offset - 3;
                this.state = getLE24(buf.rawAdd(offset - 3)).and(0x3FFFFF);
            } else {
                return Status.ioError("Invalid buffer offset: " + x);
            }
            this.state = this.state.add(DRACO_ANS_L_BASE);
            if (this.state.ge(DRACO_ANS_L_BASE * DRACO_ANS_IO_BASE)) {
                return Status.ioError("Invalid buffer offset");
            }
            return Status.ok();
        }

        public boolean ansReadEnd() {
            return this.state.equals(DRACO_ANS_L_BASE);
        }

        public boolean ansReaderHasError() {
            return this.state.lt(DRACO_ANS_L_BASE) && this.bufOffset == 0;
        }
    }
}
