package com.mndk.bteterrarenderer.core.tile.ogc3dtiles.key;

import com.mndk.bteterrarenderer.core.util.ArrayUtil;
import com.mndk.bteterrarenderer.ogc3dtiles.math.matrix.Matrix4;
import com.mndk.bteterrarenderer.ogc3dtiles.math.volume.Sphere;
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

    public List<LocalTileNode> getIntersectionsFromTileset(Tileset tileset, Sphere playerSphere,
                                                           Matrix4 parentTilesetTransform) {

        List<LocalTileNode> result = new ArrayList<>();

        @RequiredArgsConstructor
        class Node {
            final int[] indexes;
            final Tile tile;
            final Matrix4 previousTransform;
        }

        Stack<Node> nodes = new Stack<>();
        nodes.push(new Node(new int[0], tileset.getRootTile(), parentTilesetTransform));
        do {
            Node currentNode = nodes.pop();
            int[] currentIndexes = currentNode.indexes;
            Tile currentTile = currentNode.tile;

            Matrix4 localTransform = currentTile.getTileLocalTransform();
            Matrix4 currentTransform = localTransform == null ? parentTilesetTransform :
                    parentTilesetTransform.multiply(currentTile.getTileLocalTransform()).toMatrix4();

            if(!currentTile.getBoundingVolume().intersectsSphere(playerSphere, currentTransform))
                continue;

            List<Tile> children = currentTile.getChildren();
            List<TileContentLink> contentLinks = currentTile.getContents();
            TileRefinement refinement = currentTile.getRefinement();

            if(refinement != TileRefinement.REPLACE || children.isEmpty()) {
                for(int i = 0; i < contentLinks.size(); i++) {
                    TileContentLink contentLink = contentLinks.get(i);
                    Volume volume = contentLink.getBoundingVolume();
                    if(volume != null && !volume.intersectsSphere(playerSphere, currentTransform)) continue;
                    result.add(new LocalTileNode(
                            new TileLocalKey(currentIndexes, i),
                            contentLink,
                            currentTransform
                    ));
                }
            }

            for(int i = 0; i < children.size(); i++) {
                Tile child = children.get(i);
                nodes.add(new Node(ArrayUtil.expandOne(currentIndexes, i), child, currentTransform));
            }
        } while(!nodes.isEmpty());

        return result;
    }

}
