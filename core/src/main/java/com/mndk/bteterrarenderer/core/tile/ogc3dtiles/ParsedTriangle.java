package com.mndk.bteterrarenderer.core.tile.ogc3dtiles;

import com.mndk.bteterrarenderer.mcconnector.client.graphics.shape.GraphicsTriangle;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.vertex.PosTexNorm;
import com.mndk.bteterrarenderer.ogc3dtiles.math.Cartesian3;
import lombok.Data;

@Data
class ParsedTriangle {
    public final Cartesian3[] gamePositions;
    public final float[][] tex;
    public final Cartesian3[] gameNormals;

    ParsedTriangle(ParsedPoint p1, ParsedPoint p2, ParsedPoint p3) {
        this.gamePositions = new Cartesian3[] { p1.gamePos, p2.gamePos, p3.gamePos };
        this.tex = new float[][] { p1.tex, p2.tex, p3.tex };

        if (p1.gameNormal != null && p2.gameNormal != null && p3.gameNormal != null) {
            this.gameNormals = new Cartesian3[]{p1.gameNormal, p2.gameNormal, p3.gameNormal};
            return;
        }

        // From 3.7.2.1. Overview:
        //   When normals are not specified, client implementations MUST calculate
        //   flat normals and the provided tangents (if present) MUST be ignored.
        Cartesian3 u = p2.gamePos.subtract(p1.gamePos);
        Cartesian3 v = p3.gamePos.subtract(p1.gamePos);
        Cartesian3 normal = u.cross(v);
        this.gameNormals = new Cartesian3[] { normal, normal, normal };
    }

    private PosTexNorm getGraphicsVertex(int index) {
        return new PosTexNorm(
                gamePositions[index].getX(), gamePositions[index].getY(), gamePositions[index].getZ(),
                tex[index][0], tex[index][1],
                gameNormals[index].getX(), gameNormals[index].getY(), gameNormals[index].getZ()
        );
    }

    public GraphicsTriangle<PosTexNorm> toGraphics() {
        return GraphicsTriangle.newPosTexNorm(
                this.getGraphicsVertex(0),
                this.getGraphicsVertex(1),
                this.getGraphicsVertex(2)
        );
    }
}
