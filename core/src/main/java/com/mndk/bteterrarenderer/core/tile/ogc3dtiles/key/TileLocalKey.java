package com.mndk.bteterrarenderer.core.tile.ogc3dtiles.key;

import lombok.Data;

import java.util.Arrays;

@Data
public class TileLocalKey {
    private final int[] tileIndexes;
    private final int contentIndex;

    @Override
    public String toString() {
        String result = Arrays.stream(tileIndexes).mapToObj(String::valueOf).reduce((a, b) -> a + "/" + b).orElse("");
        return result + "/" + contentIndex;
    }
}
