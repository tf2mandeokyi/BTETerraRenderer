package com.mndk.bteterrarenderer.ogc3dtiles;

import de.javagl.jgltf.model.GltfModel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.annotation.Nullable;

@Getter
@Setter
@RequiredArgsConstructor
public abstract class TileData {
    private final TileDataFormat dataFormat;

    @Nullable
    public abstract String getCopyright();

    @Nullable
    public abstract GltfModel getGltfModelInstance();
}