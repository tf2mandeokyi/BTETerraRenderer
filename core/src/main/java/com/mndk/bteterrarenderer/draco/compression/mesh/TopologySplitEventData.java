package com.mndk.bteterrarenderer.draco.compression.mesh;

import com.mndk.bteterrarenderer.datatype.number.UInt;
import lombok.Getter;
import lombok.Setter;

public class TopologySplitEventData {
    private int splitSymbolId;
    private int sourceSymbolId;
    @Getter @Setter
    private EdgeFaceName sourceEdge;

    public TopologySplitEventData() {}

    public UInt getSplitSymbolId() { return UInt.of(splitSymbolId); }
    public UInt getSourceSymbolId() { return UInt.of(sourceSymbolId); }

    public void setSplitSymbolId(UInt splitSymbolId) { this.splitSymbolId = splitSymbolId.intValue(); }
    public void setSourceSymbolId(UInt sourceSymbolId) { this.sourceSymbolId = sourceSymbolId.intValue(); }
}
