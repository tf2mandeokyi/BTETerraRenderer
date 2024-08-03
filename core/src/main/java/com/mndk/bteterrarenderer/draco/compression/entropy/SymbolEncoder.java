package com.mndk.bteterrarenderer.draco.compression.entropy;

import com.mndk.bteterrarenderer.datatype.number.UInt;
import com.mndk.bteterrarenderer.datatype.number.ULong;
import com.mndk.bteterrarenderer.draco.core.EncoderBuffer;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.datatype.vector.CppVector;

public interface SymbolEncoder {

    Status create(CppVector<ULong> frequencies, int numSymbols, EncoderBuffer buffer);

    boolean needsReverseEncoding();

    void startEncoding(EncoderBuffer buffer);

    void encodeSymbol(UInt symbol);

    void endEncoding(EncoderBuffer buffer);

}
