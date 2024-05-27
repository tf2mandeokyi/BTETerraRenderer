package com.mndk.bteterrarenderer.draco_deprecated.symbol;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Getter
@RequiredArgsConstructor
public enum TopologySymbol {
    C, // TOPOLOGY_C
    S, // TOPOLOGY_S
    L, // TOPOLOGY_L
    R, // TOPOLOGY_R
    E; // TOPOLOGY_E

    private static final Map<Integer, TopologySymbol> MAP = new HashMap<Integer, TopologySymbol>() {{
        put(0, C);
        put(1, S);
        put(3, L);
        put(5, R);
        put(7, E);
    }};

    public static TopologySymbol valueOf(int value) {
        return Objects.requireNonNull(MAP.get(value));
    }
}
