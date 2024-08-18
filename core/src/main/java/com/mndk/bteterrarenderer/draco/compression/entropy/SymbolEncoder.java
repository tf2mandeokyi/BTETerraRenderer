package com.mndk.bteterrarenderer.draco.compression.entropy;

import com.mndk.bteterrarenderer.datatype.number.UInt;
import com.mndk.bteterrarenderer.datatype.number.ULong;
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.draco.core.EncoderBuffer;
import com.mndk.bteterrarenderer.draco.core.Status;

public interface SymbolEncoder {

    Status create(Pointer<ULong> frequencies, int numSymbols, EncoderBuffer buffer);

    boolean needsReverseEncoding();

    void startEncoding(EncoderBuffer buffer);

    void encodeSymbol(UInt symbol);

    void endEncoding(EncoderBuffer buffer);

}
