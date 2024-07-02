package com.mndk.bteterrarenderer.draco.io;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.UInt;
import com.mndk.bteterrarenderer.draco.core.DataBuffer;
import com.mndk.bteterrarenderer.draco.core.DecoderBuffer;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.draco.core.StatusChain;
import lombok.experimental.UtilityClass;

import javax.annotation.Nullable;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@UtilityClass
public class DracoParserUtils {

    public void skipLine(DecoderBuffer buffer) {
        parseLine(buffer, null);
    }

    public void parseLine(DecoderBuffer buffer, @Nullable AtomicReference<String> outString) {
        if(outString != null) {
            outString.set("");
        }
        AtomicReference<Byte> cRef = new AtomicReference<>();
        int numDelims = 0;
        byte lastDelim = 0;
        while(buffer.peek(DataType.int8(), cRef::set).isOk()) {
            byte c = cRef.get();
            boolean isDelim = (c == '\r' || c == '\n');
            if(isDelim) {
                if(numDelims == 0) {
                    lastDelim = c;
                } else if(numDelims == 1) {
                    if(c == lastDelim || c != '\n') {
                        return;
                    }
                } else {
                    return;
                }
                numDelims++;
            }
            if(!isDelim && numDelims > 0) {
                return;
            }
            buffer.advance(1);
            if(!isDelim && outString != null) {
                outString.set(outString.get() + c);
            }
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
        AtomicReference<Byte> cRef = new AtomicReference<>();
        if(buffer.peek(DataType.int8(), cRef::set).isError()) {
            endReached.set(true);
            return false; // eof reached.
        }
        byte c = cRef.get();
        return c == ' ' || c == '\t' || c == '\n' || c == '\r' || c == '\f' || c == '\u000B';
    }

    public Status parseFloat(DecoderBuffer buffer, AtomicReference<Float> out) {
        StatusChain chain = new StatusChain();

        AtomicReference<Byte> chRef = new AtomicReference<>();
        if(buffer.peek(DataType.int8(), chRef::set).isError(chain)) return chain.get();
        byte ch = chRef.get();
        int sign = getSignValue(ch);
        if(sign != 0) {
            buffer.advance(1);
        } else {
            sign = 1;
        }

        boolean haveDigits = false;
        double v = 0.0;
        while(buffer.peek(DataType.int8(), chRef::set).isOk()) {
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
            while(buffer.peek(DataType.int8(), chRef::set).isOk()) {
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
                if(!parseSignedInt(buffer, exponentRef).isError(chain)) return chain.get();
                int exponent = exponentRef.get();
                v *= Math.pow(10.0, exponent);
            }
        }

        out.set((float) (sign < 0 ? -v : v));
        return Status.ok();
    }

    public static Status parseSignedInt(DecoderBuffer buffer, AtomicReference<Integer> out) {
        StatusChain chain = new StatusChain();

        AtomicReference<Byte> chRef = new AtomicReference<>();
        if(buffer.peek(DataType.int8(), chRef::set).isError(chain)) return chain.get();
        int sign = getSignValue(chRef.get());
        if(sign != 0) {
            buffer.advance(1);
        }

        AtomicReference<UInt> vRef = new AtomicReference<>();
        if(!parseUnsignedInt(buffer, vRef).isError(chain)) return chain.get();
        int v = vRef.get().intValue();

        out.set(sign < 0 ? -v : v);
        return Status.ok();
    }

    public static Status parseUnsignedInt(DecoderBuffer buffer, AtomicReference<UInt> out) {
        AtomicReference<UInt> vRef = new AtomicReference<>(UInt.of(0));
        AtomicReference<Byte> chRef = new AtomicReference<>();
        boolean haveDigits = false;
        while(buffer.peek(DataType.int8(), chRef::set).isOk()) {
            byte ch = chRef.get();
            if(ch < '0' || ch > '9') break;

            UInt v = vRef.get();
            v = v.mul(10);
            v = v.add(ch - '0');
            vRef.set(v);
            buffer.advance(1);
            haveDigits = true;
        }
        if(!haveDigits) {
            return Status.ioError("No digits found");
        }
        out.set(vRef.get());
        return Status.ok();
    }

    public static Status parseString(DecoderBuffer buffer, AtomicReference<String> out) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(0);
        skipWhitespace(buffer);
        AtomicBoolean endReached = new AtomicBoolean();
        while(!peekWhitespace(buffer, endReached) && !endReached.get()) {
            AtomicReference<Byte> cRef = new AtomicReference<>();
            if(buffer.decode(DataType.int8(), cRef::set).isError()) {
                return Status.ioError("Failed to decode string");
            }
            byte c = cRef.get();
            byteBuffer.put(c);
        }
        out.set(new String(byteBuffer.array(), StandardCharsets.UTF_8));
        return Status.ok();
    }

    public static DecoderBuffer parseLineIntoDecoderBuffer(DecoderBuffer buffer) {
        DataBuffer head = buffer.getDataHead();
        long size = 0;
        AtomicReference<Byte> cRef = new AtomicReference<>();
        while(buffer.peek(DataType.int8(), cRef::set).isOk()) {
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

    public static void skipCharacters(DecoderBuffer buffer, String skipString) {
        if(skipString == null) return;
        int numSkipChars = skipString.length();
        AtomicReference<Byte> cRef = new AtomicReference<>();
        while(buffer.peek(DataType.int8(), cRef::set).isOk()) {
            byte c = cRef.get();
            boolean skip = false;
            for(int i = 0; i < numSkipChars; ++i) {
                if(c == skipString.charAt(i)) {
                    skip = true;
                    break;
                }
            }
            if(!skip) {
                return;
            }
            buffer.advance(1);
        }
    }
}
