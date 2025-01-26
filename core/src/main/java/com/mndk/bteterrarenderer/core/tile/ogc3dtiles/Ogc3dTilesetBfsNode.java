package com.mndk.bteterrarenderer.core.tile.ogc3dtiles;

import com.mndk.bteterrarenderer.core.tile.ogc3dtiles.key.LocalTileNode;
import com.mndk.bteterrarenderer.core.tile.ogc3dtiles.key.TileLocalKey;
import com.mndk.bteterrarenderer.ogc3dtiles.math.BoundingSphere;
import com.mndk.bteterrarenderer.ogc3dtiles.math.SpheroidFrustum;
import com.mndk.bteterrarenderer.ogc3dtiles.math.volume.Volume;
import com.mndk.bteterrarenderer.ogc3dtiles.tile.Tile;
import com.mndk.bteterrarenderer.ogc3dtiles.tile.TileContentLink;
import com.mndk.bteterrarenderer.ogc3dtiles.tile.TileRefinement;
import com.mndk.bteterrarenderer.ogc3dtiles.tile.Tileset;
import com.mndk.bteterrarenderer.util.ArrayUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.joml.Matrix4d;
import org.joml.Vector3d;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

@Getter
@RequiredArgsConstructor
public class Ogc3dTilesetBfsNode {
    private final Ogc3dTileMapService tms;
    private final Tileset tileset;
    private final URL parentUrl;
    private final TileLocalKey[] parentKeys;
    private final Matrix4d parentTilesetTransform;

    public List<LocalTileNode> selectIntersections(SpheroidFrustum frustum) {

        List<LocalTileNode> resultList = new ArrayList<>();
        Stack<IntersectionBfsNode> nodeStack = new Stack<>();
        nodeStack.push(new IntersectionBfsNode(new int[0], this.tileset.getRootTile()));

        boolean renderSurroundings = tms.isRenderSurroundings();

        while (!nodeStack.isEmpty()) {
            IntersectionBfsNode currentNode = nodeStack.pop();
            int[] currentIndexes = currentNode.indexes;
            Tile currentTile = currentNode.tile;

            if (!renderSurroundings && !this.shouldIncludeTile(currentTile, frustum)) continue;

            boolean atLeastOneChildIntersects = this.atLeastOneChildIntersects(currentTile, frustum);
            boolean includeCurrentTileContent = !atLeastOneChildIntersects
                    || currentTile.getRefinement() == TileRefinement.ADD;

            if (includeCurrentTileContent) {
                this.addContentsToResult(resultList, currentTile, currentIndexes);
            }
            if (atLeastOneChildIntersects) {
                this.addChildrenToStack(nodeStack, currentTile, currentIndexes);
            }
        }

        return resultList;
    }

    private void addContentsToResult(List<LocalTileNode> resultList, Tile tile, int[] indexes) {
        for (int i = 0; i < tile.getContents().size(); i++) {
            TileLocalKey contentKey = new TileLocalKey(indexes, i);
            TileContentLink content = tile.getContents().get(i);
            Matrix4d transform = tile.getGlobalTransform(this.parentTilesetTransform);
            resultList.add(new LocalTileNode(contentKey, content, transform));
        }
    }

    private void addChildrenToStack(Stack<IntersectionBfsNode> stack, Tile tile, int[] indexes) {
        for (int i = 0; i < tile.getChildren().size(); i++) {
            Tile child = tile.getChildren().get(i);
            stack.add(new IntersectionBfsNode(ArrayUtil.expandOne(indexes, i), child));
        }
    }

    private boolean atLeastOneChildIntersects(Tile currentTile, SpheroidFrustum frustum) {
        for (Tile child : currentTile.getChildren()) {
            if (this.shouldIncludeTile(child, frustum)) return true;
        }
        return false;
    }

    public TileLocalKey[] attachKey(LocalTileNode node) {
        return ArrayUtil.expandOne(this.parentKeys, node.getKey(), TileLocalKey[]::new);
    }

    private boolean shouldIncludeTile(Tile tile, SpheroidFrustum frustum) {
        Volume volume = tile.getBoundingVolume();
        Matrix4d transform = tile.getGlobalTransform(this.parentTilesetTransform);
        boolean intersects = frustum.intersectsVolume(volume, transform, tms.getCoordConverter());
        if (!intersects) return false;

        double lodFactor = tms.getLodFactor(); // ranges from 0 to 5
        double lodFactorPow = Math.pow(2, lodFactor); // ranges from 1 to 32

        BoundingSphere lodSphere = volume.getLevelOfDetailSphere(transform, tms.getCoordConverter());
        Vector3d sphereCenter = lodSphere.getCenter();
        double sphereRadius = lodSphere.getRadius();

        Vector3d cameraPosition = frustum.getCameraPosition();
        double distance = sphereCenter.distance(cameraPosition);
        double effectiveDistance = Math.max(distance - sphereRadius, 0);

        double geometricError = tile.getGeometricError();
        return effectiveDistance < geometricError * lodFactorPow;
    }

    public static Ogc3dTilesetBfsNode fromRoot(Ogc3dTileMapService tms, Tileset tileset, URL parentUrl) {
        return new Ogc3dTilesetBfsNode(tms, tileset, parentUrl, new TileLocalKey[0], new Matrix4d());
    }

    @RequiredArgsConstructor
    private static class IntersectionBfsNode {
        private final int[] indexes;
        private final Tile tile;
    }
}
