package com.mndk.bteterrarenderer.connector.netty;

import java.nio.charset.Charset;

public interface IByteBuf {
    int readInt();
    CharSequence readCharSequence(int strLength, Charset utf8);

    void writeInt(int length);
    void writeCharSequence(CharSequence projectionJson, Charset utf8);
}
