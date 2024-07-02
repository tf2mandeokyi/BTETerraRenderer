package com.mndk.bteterrarenderer.draco.compression.mesh;

import com.mndk.bteterrarenderer.datatype.number.UInt;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum EdgebreakerTopology {
    C(0x0, 0), //     0
    S(0x1, 1), // 0 0 1
    L(0x3, 2), // 0 1 1
    R(0x5, 3), // 1 0 1
    E(0x7, 4), // 1 1 1
    /** A special value used to indicate an invalid symbol. */
    INVALID(-1, -1);

    private final int topologyBitPattern;
    private final int id;

    private static final EdgebreakerTopology[] BIT_PATTERN_TO_TOPOLOGY = { C, S, INVALID, L, INVALID, R, INVALID, E };
    public static EdgebreakerTopology fromBitPattern(int bitPattern) { return BIT_PATTERN_TO_TOPOLOGY[bitPattern]; }
    public static EdgebreakerTopology fromBitPattern(UInt bitPattern) { return fromBitPattern(bitPattern.intValue()); }

    private static final EdgebreakerTopology[] SYMBOL_TO_TOPOLOGY = { C, S, L, R, E };
    public static EdgebreakerTopology fromSymbol(int symbol) { return SYMBOL_TO_TOPOLOGY[symbol]; }
    public static EdgebreakerTopology fromSymbol(UInt symbol) { return fromSymbol(symbol.intValue()); }
}
