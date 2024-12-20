package com.mndk.bteterrarenderer.core.tile.ogc3dtiles.key;

import com.mndk.bteterrarenderer.core.tile.ogc3dtiles.Ogc3dTileMapService;
import com.mndk.bteterrarenderer.util.ArrayUtil;
import com.mndk.bteterrarenderer.ogc3dtiles.math.SpheroidCoordinatesConverter;
import com.mndk.bteterrarenderer.ogc3dtiles.math.SpheroidFrustum;
import com.mndk.bteterrarenderer.ogc3dtiles.math.matrix.Matrix4f;
import com.mndk.bteterrarenderer.ogc3dtiles.math.volume.Volume;
import com.mndk.bteterrarenderer.ogc3dtiles.tile.Tile;
import com.mndk.bteterrarenderer.ogc3dtiles.tile.TileContentLink;
import com.mndk.bteterrarenderer.ogc3dtiles.tile.TileRefinement;
import com.mndk.bteterrarenderer.ogc3dtiles.tile.Tileset;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

@UtilityClass
public class TileKeyManager {

    public List<LocalTileNode> getIntersectionsFromTileset(Tileset tileset, Matrix4f parentTilesetTransform,
                                                           SpheroidFrustum frustum, Ogc3dTileMapService tms) {
        @RequiredArgsConstructor
        class Node {
            final int[] indexes;
            final Tile tile;
            final Matrix4f previousTransform;
        }

        List<LocalTileNode> resultList = new ArrayList<>();
        Stack<Node> nodeStack = new Stack<>();
        nodeStack.push(new Node(new int[0], tileset.getRootTile(), parentTilesetTransform));

        boolean renderSurroundings = tms.isRenderSurroundings();
        SpheroidCoordinatesConverter converter = tms.getCoordConverter();

        while (!nodeStack.isEmpty()) {
            Node currentNode = nodeStack.pop();
            int[] currentIndexes = currentNode.indexes;
            Tile currentTile = currentNode.tile;

            Matrix4f localTransform = currentTile.getTileLocalTransform();
            Matrix4f currentTransform = currentNode.previousTransform;
            if (localTransform != null) currentTransform = currentTransform.multiply(localTransform);

            Volume boundingVolume = currentTile.getBoundingVolume();
            if (!renderSurroundings && !frustum.intersectsVolume(boundingVolume, currentTransform, converter))
                continue;

            List<Tile> children = currentTile.getChildren();
            List<TileContentLink> currentTileContents = currentTile.getContents();

            boolean atLeastOneChildIntersects = false;
            for (Tile child : children) {
                Volume childBoundingVolume = child.getBoundingVolume();
                if (frustum.intersectsVolume(childBoundingVolume, currentTransform, converter)) {
                    atLeastOneChildIntersects = true;
                    break;
                }
            }

            boolean includeCurrentTileContent = !atLeastOneChildIntersects
                    || currentTile.getRefinement() == TileRefinement.ADD;

            if (includeCurrentTileContent) {
                for (int i = 0; i < currentTileContents.size(); i++) {
                    TileLocalKey contentKey = new TileLocalKey(currentIndexes, i);
                    TileContentLink content = currentTileContents.get(i);
                    LocalTileNode currentTileContentNode = new LocalTileNode(contentKey, content, currentTransform);
                    resultList.add(currentTileContentNode);
                }
            }
            if (atLeastOneChildIntersects) {
                for (int i = 0; i < children.size(); i++) {
                    Tile child = children.get(i);
                    nodeStack.add(new Node(ArrayUtil.expandOne(currentIndexes, i), child, currentTransform));
                }
            }
        }

        return resultList;
    }

}
