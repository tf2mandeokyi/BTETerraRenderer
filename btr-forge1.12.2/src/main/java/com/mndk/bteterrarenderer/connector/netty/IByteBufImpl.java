package com.mndk.bteterrarenderer.connector.netty;

import io.netty.buffer.ByteBuf;
import lombok.RequiredArgsConstructor;

import java.nio.charset.Charset;

@RequiredArgsConstructor
public class IByteBufImpl implements IByteBuf {
    private final ByteBuf byteBuf;

    public int readInt() { return byteBuf.readInt(); }
    public CharSequence readCharSequence(int strLength, Charset utf8) { return byteBuf.readCharSequence(strLength, utf8); }

    public void writeInt(int length) { byteBuf.writeInt(length); }
    public void writeCharSequence(CharSequence projectionJson, Charset utf8) { byteBuf.writeCharSequence(projectionJson, utf8); }
}
