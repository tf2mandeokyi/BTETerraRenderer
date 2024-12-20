package com.mndk.bteterrarenderer.core.tile.ogc3dtiles;

import com.mndk.bteterrarenderer.core.tile.ogc3dtiles.key.LocalTileNode;
import com.mndk.bteterrarenderer.core.tile.ogc3dtiles.key.TileLocalKey;
import com.mndk.bteterrarenderer.ogc3dtiles.math.BoundingSphere;
import com.mndk.bteterrarenderer.ogc3dtiles.math.Cartesian3f;
import com.mndk.bteterrarenderer.ogc3dtiles.math.SpheroidFrustum;
import com.mndk.bteterrarenderer.ogc3dtiles.math.matrix.Matrix4f;
import com.mndk.bteterrarenderer.ogc3dtiles.math.volume.Volume;
import com.mndk.bteterrarenderer.ogc3dtiles.tile.Tile;
import com.mndk.bteterrarenderer.ogc3dtiles.tile.TileContentLink;
import com.mndk.bteterrarenderer.ogc3dtiles.tile.TileRefinement;
import com.mndk.bteterrarenderer.ogc3dtiles.tile.Tileset;
import com.mndk.bteterrarenderer.util.ArrayUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

@Getter
@RequiredArgsConstructor
public class Ogc3dBfsNode {
    private final Ogc3dTileMapService tms;
    private final Tileset tileset;
    private final URL parentUrl;
    private final TileLocalKey[] parentKeys;
    private final Matrix4f parentTransform;

    public List<LocalTileNode> selectIntersections(SpheroidFrustum frustum) {

        List<LocalTileNode> resultList = new ArrayList<>();
        Stack<IntersectionBfsNode> nodeStack = new Stack<>();
        nodeStack.push(new IntersectionBfsNode(new int[0], this.tileset.getRootTile(), this.parentTransform));

        boolean renderSurroundings = tms.isRenderSurroundings();

        while (!nodeStack.isEmpty()) {
            IntersectionBfsNode currentNode = nodeStack.pop();
            int[] currentIndexes = currentNode.indexes;
            Tile currentTile = currentNode.tile;

            Matrix4f localTransform = currentTile.getTileLocalTransform();
            Matrix4f currentTransform = currentNode.previousTransform;
            if (localTransform != null) currentTransform = currentTransform.multiply(localTransform);

            if (!renderSurroundings && !this.shouldIncludeTile(currentTile, frustum, currentTransform))
                continue;

            boolean atLeastOneChildIntersects = this.atLeastOneChildIntersects(currentTile, frustum, currentTransform);
            boolean includeCurrentTileContent = !atLeastOneChildIntersects
                    || currentTile.getRefinement() == TileRefinement.ADD;

            if (includeCurrentTileContent) {
                this.addContentsToResult(resultList, currentTile, currentIndexes, currentTransform);
            }
            if (atLeastOneChildIntersects) {
                this.addChildrenToStack(nodeStack, currentTile, currentIndexes, currentTransform);
            }
        }

        return resultList;
    }

    private void addContentsToResult(List<LocalTileNode> resultList, Tile tile, int[] indexes, Matrix4f transform) {
        for (int i = 0; i < tile.getContents().size(); i++) {
            TileLocalKey contentKey = new TileLocalKey(indexes, i);
            TileContentLink content = tile.getContents().get(i);
            resultList.add(new LocalTileNode(contentKey, content, transform));
        }
    }

    private void addChildrenToStack(Stack<IntersectionBfsNode> stack, Tile tile, int[] indexes, Matrix4f previousTransform) {
        for (int i = 0; i < tile.getChildren().size(); i++) {
            Tile child = tile.getChildren().get(i);
            stack.add(new IntersectionBfsNode(ArrayUtil.expandOne(indexes, i), child, previousTransform));
        }
    }

    private boolean atLeastOneChildIntersects(Tile currentTile, SpheroidFrustum frustum, Matrix4f currentTransform) {
        for (Tile child : currentTile.getChildren()) {
            if (this.shouldIncludeTile(child, frustum, currentTransform)) {
                return true;
            }
        }
        return false;
    }

    public TileLocalKey[] attachKey(LocalTileNode node) {
        return ArrayUtil.expandOne(this.parentKeys, node.getKey(), TileLocalKey[]::new);
    }

    private boolean shouldIncludeTile(Tile tile, SpheroidFrustum frustum, Matrix4f transform) {
        Volume volume = tile.getBoundingVolume();
        boolean intersects = frustum.intersectsVolume(volume, transform, tms.getCoordConverter());
        if (!intersects) return false;

        double lodFactor = tms.getLodFactor(); // ranges from 0.5 to 2.0
        double geometricError = tile.getGeometricError();

        BoundingSphere lodSphere = volume.getLevelOfDetailSphere(transform, tms.getCoordConverter());
        Cartesian3f sphereCenter = lodSphere.getCenter();
        double sphereRadius = lodSphere.getRadius();

        Cartesian3f cameraPosition = frustum.getCameraPosition();
        double distance = sphereCenter.subtract(cameraPosition).distance();
        double effectiveDistance = Math.max(distance - sphereRadius, 0);

        return effectiveDistance * lodFactor < geometricError;
    }

    public static Ogc3dBfsNode fromRoot(Ogc3dTileMapService tms, Tileset tileset, URL parentUrl) {
        return new Ogc3dBfsNode(tms, tileset, parentUrl, new TileLocalKey[0], Matrix4f.IDENTITY);
    }

    @RequiredArgsConstructor
    private static class IntersectionBfsNode {
        private final int[] indexes;
        private final Tile tile;
        private final Matrix4f previousTransform;
    }
}
