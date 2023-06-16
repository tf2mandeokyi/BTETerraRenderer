package com.mndk.bteterrarenderer.graphics;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class GraphicsModel {
    private final int textureGlId;
    private final List<GraphicsQuad<GraphicsQuad.PosTexColor>> quads;
}
