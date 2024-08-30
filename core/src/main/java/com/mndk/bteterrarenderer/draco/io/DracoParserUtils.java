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

package com.mndk.bteterrarenderer.draco.io;

import com.mndk.bteterrarenderer.datatype.number.UInt;
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.datatype.pointer.RawPointer;
import com.mndk.bteterrarenderer.draco.core.DecoderBuffer;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.draco.core.StatusChain;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.experimental.UtilityClass;

import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@UtilityClass
public class DracoParserUtils {

    public void skipLine(DecoderBuffer buffer) {
        parseLine(buffer, null);
    }

    public void parseLine(DecoderBuffer buffer, @Nullable AtomicReference<String> outString) {
        ByteBuf buf = Unpooled.buffer();
        Pointer<Byte> cRef = Pointer.newByte();
        int numDelims = 0;
        byte lastDelim = 0;
        while(buffer.peek(cRef).isOk()) {
            byte c = cRef.get();
            boolean isDelim = (c == '\r' || c == '\n');
            if(isDelim) {
                if(numDelims == 0) {
                    lastDelim = c;
                } else if(numDelims == 1) {
                    if(c == lastDelim || c != '\n') break;
                } else break;
                numDelims++;
            }
            if(!isDelim && numDelims > 0) break;
            buffer.advance(1);
            if(!isDelim && outString != null) buf.writeByte(c);
        }
        if(outString != null) {
            outString.set(buf.toString(StandardCharsets.UTF_8));
        }
    }

    public void skipWhitespace(DecoderBuffer buffer) {
        AtomicBoolean endReached = new AtomicBoolean();
        while(peekWhitespace(buffer, endReached) && !endReached.get()) {
            // Skip the whitespace character
            buffer.advance(1);
        }
    }

    public boolean peekWhitespace(DecoderBuffer buffer, AtomicBoolean endReached) {
        Pointer<Byte> cRef = Pointer.newByte();
        if(buffer.peek(cRef).isError()) {
            endReached.set(true);
            return false; // eof reached.
        }
        byte c = cRef.get();
        return c == ' ' || c == '\t' || c == '\n' || c == '\r' || c == '\f' || c == '\u000B';
    }

    public Status parseFloat(DecoderBuffer buffer, AtomicReference<Float> out) {
        StatusChain chain = new StatusChain();

        Pointer<Byte> chRef = Pointer.newByte();
        if(buffer.peek(chRef).isError(chain)) return chain.get();
        byte ch = chRef.get();
        int sign = getSignValue(ch);
        if(sign != 0) {
            buffer.advance(1);
        } else {
            sign = 1;
        }

        boolean haveDigits = false;
        double v = 0.0;
        while(buffer.peek(chRef).isOk()) {
            ch = chRef.get();
            if(ch < '0' || ch > '9') break;

            v *= 10.0;
            v += (ch - '0');
            buffer.advance(1);
            haveDigits = true;
        }
        if(ch == '.') {
            buffer.advance(1);
            double fraction = 1.0;
            while(buffer.peek(chRef).isOk()) {
                ch = chRef.get();
                if(ch < '0' || ch > '9') break;

                fraction *= 0.1;
                v += (ch - '0') * fraction;
                buffer.advance(1);
                haveDigits = true;
            }
        }

        if(!haveDigits) {
            AtomicReference<String> textRef = new AtomicReference<>();
            if(parseString(buffer, textRef).isError(chain)) return chain.get();
            String text = textRef.get();
            if(text.equals("inf") || text.equals("Inf")) v = Double.POSITIVE_INFINITY;
            else if(text.equals("nan") || text.equals("NaN")) v = Double.NaN;
            else return Status.ioError("Invalid string: " + text);
        } else {
            if(ch == 'e' || ch == 'E') {
                buffer.advance(1);
                AtomicReference<Integer> exponentRef = new AtomicReference<>();
                if(parseSignedInt(buffer, exponentRef).isError(chain)) return chain.get();
                int exponent = exponentRef.get();
                v *= Math.pow(10.0, exponent);
            }
        }

        out.set((float) (sign < 0 ? -v : v));
        return Status.ok();
    }

    public static Status parseSignedInt(DecoderBuffer buffer, AtomicReference<Integer> out) {
        StatusChain chain = new StatusChain();

        Pointer<Byte> chRef = Pointer.newByte();
        if(buffer.peek(chRef).isError(chain)) return chain.get();
        int sign = getSignValue(chRef.get());
        if(sign != 0) {
            buffer.advance(1);
        }

        AtomicReference<UInt> vRef = new AtomicReference<>();
        if(parseUnsignedInt(buffer, vRef).isError(chain)) return chain.get();
        int v = vRef.get().intValue();

        out.set(sign < 0 ? -v : v);
        return Status.ok();
    }

    public static Status parseUnsignedInt(DecoderBuffer buffer, AtomicReference<UInt> out) {
        UInt v = UInt.ZERO;
        Pointer<Byte> chRef = Pointer.newByte();
        boolean haveDigits = false;
        while(buffer.peek(chRef).isOk()) {
            byte ch = chRef.get();
            if(ch < '0' || ch > '9') break;

            v = v.mul(10);
            v = v.add(ch - '0');
            buffer.advance(1);
            haveDigits = true;
        }
        if(!haveDigits) {
            return Status.ioError("No digits found");
        }
        out.set(v);
        return Status.ok();
    }

    public static Status parseString(DecoderBuffer buffer, AtomicReference<String> out) {
        ByteBuf buf = Unpooled.buffer(0);
        skipWhitespace(buffer);
        AtomicBoolean endReached = new AtomicBoolean();
        while(!peekWhitespace(buffer, endReached) && !endReached.get()) {
            Pointer<Byte> cRef = Pointer.newByte();
            if(buffer.decode(cRef).isError()) {
                return Status.ioError("Failed to decode string");
            }
            byte c = cRef.get();
            buf.writeByte(c);
        }
        out.set(buf.toString(StandardCharsets.UTF_8));
        return Status.ok();
    }

    public static DecoderBuffer parseLineIntoDecoderBuffer(DecoderBuffer buffer) {
        RawPointer head = buffer.getDataHead();
        long size = 0;
        Pointer<Byte> cRef = Pointer.newByte();
        while(buffer.peek(cRef).isOk()) {
            buffer.advance(1);
            size++;
            byte c = cRef.get();
            if(c == '\n') break; // End of the line reached.
        }
        DecoderBuffer outBuffer = new DecoderBuffer();
        outBuffer.init(head, size);
        return outBuffer;
    }

    private int getSignValue(byte ch) {
        return ch == '-' ? -1 : ch == '+' ? 1 : 0;
    }

    public static void skipCharacters(DecoderBuffer buffer, byte[] skipChars) {
        if(skipChars == null) return;
        Pointer<Byte> cRef = Pointer.newByte();
        while(buffer.peek(cRef).isOk()) {
            byte c = cRef.get();
            boolean skip = false;
            for(byte skipChar : skipChars) {
                if(c != skipChar) continue;
                skip = true;
                break;
            }
            if(!skip) return;
            buffer.advance(1);
        }
    }
}
