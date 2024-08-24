package com.mndk.bteterrarenderer.core.tile.ogc3dtiles.key;

import lombok.Data;

import java.util.Arrays;

@Data
public class TileGlobalKey {
    public static final TileGlobalKey ROOT_KEY = new TileGlobalKey(new TileLocalKey[0]);

    private final TileLocalKey[] keys;

    @Override
    public String toString() {
        String result = Arrays.stream(keys).map(TileLocalKey::toString).reduce((a, b) -> a + ", " + b).orElse("");
        return "TileGlobalKey[" + result + "]";
    }
}
