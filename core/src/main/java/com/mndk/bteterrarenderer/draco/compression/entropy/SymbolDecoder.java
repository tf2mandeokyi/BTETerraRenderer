package com.mndk.bteterrarenderer.draco.compression.entropy;

import com.mndk.bteterrarenderer.datatype.number.UInt;
import com.mndk.bteterrarenderer.draco.core.DecoderBuffer;
import com.mndk.bteterrarenderer.draco.core.Status;

public interface SymbolDecoder {

    Status create(DecoderBuffer buffer);

    Status startDecoding(DecoderBuffer buffer);

    UInt getNumSymbols();

    UInt decodeSymbol();

    void endDecoding();

}
