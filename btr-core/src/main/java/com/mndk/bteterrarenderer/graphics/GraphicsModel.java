package com.mndk.bteterrarenderer.graphics;

import lombok.Data;

import java.util.List;

@Data
public class GraphicsModel {
    private final int textureGlId;
    private final List<GraphicsQuad<GraphicsQuad.PosTexColor>> quads;
}
