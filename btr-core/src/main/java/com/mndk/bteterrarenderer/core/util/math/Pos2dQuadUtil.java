package com.mndk.bteterrarenderer.core.util.math;

import com.mndk.bteterrarenderer.core.graphics.GraphicsQuad;
import com.mndk.bteterrarenderer.core.graphics.GraphicsQuad.Pos;
import lombok.experimental.UtilityClass;

import javax.annotation.Nullable;
import java.util.*;

@UtilityClass
public class Pos2dQuadUtil {

    public List<GraphicsQuad<Pos>> clipQuad(GraphicsQuad<Pos> a, GraphicsQuad<Pos> b) {
        Pos a0 = a.getVertex(0), a1 = a.getVertex(1), a2 = a.getVertex(2), a3 = a.getVertex(3);
        Pos b0 = b.getVertex(0), b1 = b.getVertex(1), b2 = b.getVertex(2), b3 = b.getVertex(3);

        return new ArrayList<GraphicsQuad<Pos>>() {{
            addAll(clipTriangles(new Pos[] { a0, a1, a2 }, new Pos[] { b0, b1, b2 }));
            addAll(clipTriangles(new Pos[] { a2, a3, a0 }, new Pos[] { b0, b1, b2 }));
            addAll(clipTriangles(new Pos[] { a0, a1, a2 }, new Pos[] { b2, b3, b0 }));
            addAll(clipTriangles(new Pos[] { a2, a3, a0 }, new Pos[] { b2, b3, b0 }));
        }};
    }
    
    private List<GraphicsQuad<Pos>> clipTriangles(Pos[] t1, Pos[] t2) {
        Pos[] clipResult = clipPolygon(t1, t2);
        if(clipResult.length < 3) return Collections.emptyList();
        List<GraphicsQuad<Pos>> quadList = new ArrayList<>();

        Pos[] quadBuffer = new Pos[3];
        int bufferLength = 0;
        for(int i = 1; i < clipResult.length; i++) {
            quadBuffer[bufferLength++] = clipResult[i];
            if(bufferLength == 3) {
                quadList.add(GraphicsQuad.newPosQuad(
                        clipResult[0], quadBuffer[0], quadBuffer[1], quadBuffer[2]
                ));
                quadBuffer[0] = clipResult[i];
                bufferLength = 1;
            }
        }
        
        // Add the leftover "triangle"
        if(bufferLength == 2) {
            quadList.add(GraphicsQuad.newPosQuad(
                    clipResult[0], quadBuffer[0], quadBuffer[1], clipResult[0]
            ));
        }
        
        return quadList;
    }
    
    /**
     * @link <a href="https://en.wikipedia.org/wiki/Sutherland%E2%80%93Hodgman_algorithm">Sutherland–Hodgman Algorithm</a>
     */
    private Pos[] clipPolygon(Pos[] polygon,
                                           Pos[] clipConvex) {
        Pos clipConvexCenter = getPolygonCenter(clipConvex);
        Pos[] vertexArray = polygon;

        // Iterate through all clipConvex edges
        for(int i = 0; i < clipConvex.length; i++) {
            Pos edgeV0 = clipConvex[i];
            Pos edgeV1 = clipConvex[(i+1) % clipConvex.length];

            List<Pos> newVertexList = new ArrayList<>();
            // Iterate through all vertexArray edges
            for(int j = 0; j < vertexArray.length; j++) {
                Pos currentPoint = vertexArray[i];
                Pos prevPoint = vertexArray[(((i-1) % vertexArray.length) + vertexArray.length) % vertexArray.length];

                Pos intersectingPoint = getLineIntersection(prevPoint, currentPoint, edgeV0, edgeV1);
                if(isPointInsideEdge(clipConvexCenter, edgeV0, edgeV1, currentPoint)) {
                    if(!isPointInsideEdge(clipConvexCenter, edgeV0, edgeV1, prevPoint)) {
                        newVertexList.add(intersectingPoint);
                    }
                    newVertexList.add(currentPoint);
                }
                else if(isPointInsideEdge(clipConvexCenter, edgeV0, edgeV1, prevPoint)) {
                    newVertexList.add(intersectingPoint);
                }
            }

            vertexArray = newVertexList.toArray(new Pos[0]);
        }

        return vertexArray;
    }

    private Pos getPolygonCenter(Pos[] polygon) {
        float x = 0, y = 0;
        for (Pos pos : polygon) {
            x += pos.x; y += pos.y;
        }
        return new Pos(x / polygon.length, y / polygon.length, 0);
    }

    private boolean isPointInsideEdge(Pos convexCenter,
                                      Pos v0, Pos v1,
                                      Pos point) {
        float centerCross = zCross(v0, v1, convexCenter);
        float pointCross = zCross(v0, v1, point);
        return centerCross * pointCross > 0;
    }

    private float zCross(Pos center, Pos v0, Pos v1) {
        float x0 = v0.x - center.x, y0 = v0.y - center.y;
        float x1 = v1.x - center.x, y1 = v1.y - center.y;
        return x0 * y1 - x1 * y0;
    }

    @Nullable
    private Pos getLineIntersection(Pos p0, Pos p1,
                                                 Pos q0, Pos q1) {
        float divisor = (p0.x - p1.x) * (q0.y - q1.y) - (p0.y - p1.y) * (q0.x - q1.x);
        if(divisor == 0) return null;

        float s = ((p0.x - q0.x) * (q0.y - q1.y) - (p0.y - q0.y) * (q0.x - q1.x)) / divisor;
        float t = ((p0.x - q0.x) * (p0.y - p1.y) - (p0.y - q0.y) * (p0.x - p1.x)) / divisor;
        if(s < 0 || s > 1 || t < 0 || t > 1) return null;

        return new Pos(p0.x + t * (p1.x - p0.x), p0.y + t * (p1.y - p0.y), 0);
    }

}
