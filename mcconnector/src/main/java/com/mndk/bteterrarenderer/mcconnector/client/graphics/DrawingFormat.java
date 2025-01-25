package com.mndk.bteterrarenderer.mcconnector.client.graphics;

import com.mndk.bteterrarenderer.mcconnector.client.graphics.shape.GraphicsQuad;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.shape.GraphicsShape;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.shape.GraphicsTriangle;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.vertex.PosTex;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.vertex.PosTexNorm;

public interface DrawingFormat<S extends GraphicsShape> {

    DrawingFormat<GraphicsQuad<PosTex>> QUAD_PT = VertexBeginner::begin3dQuad;
    DrawingFormat<GraphicsTriangle<PosTexNorm>> TRI_PTN = VertexBeginner::begin3dTri;

    BufferBuilderWrapper<S> begin(VertexBeginner beginner, NativeTextureWrapper texture);
}
