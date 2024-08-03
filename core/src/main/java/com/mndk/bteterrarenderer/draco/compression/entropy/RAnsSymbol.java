package com.mndk.bteterrarenderer.draco.compression.entropy;

import com.mndk.bteterrarenderer.datatype.number.UInt;
import lombok.ToString;

@ToString
public class RAnsSymbol {
    public UInt prob, cumProb;
}
