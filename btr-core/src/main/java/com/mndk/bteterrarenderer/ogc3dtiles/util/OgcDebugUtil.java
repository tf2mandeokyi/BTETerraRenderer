package com.mndk.bteterrarenderer.ogc3dtiles.util;

import com.mndk.bteterrarenderer.core.util.BtrUtil;
import com.mndk.bteterrarenderer.ogc3dtiles.Constants;
import com.mndk.bteterrarenderer.ogc3dtiles.TileData;
import com.mndk.bteterrarenderer.ogc3dtiles.math.matrix.Matrix4;
import com.mndk.bteterrarenderer.ogc3dtiles.tile.Tile;
import com.mndk.bteterrarenderer.ogc3dtiles.tile.Tileset;
import com.mndk.bteterrarenderer.ogc3dtiles.tile.TileContentLink;
import com.mndk.bteterrarenderer.ogc3dtiles.tile.TileRefinement;
import de.javagl.jgltf.model.*;
import de.javagl.jgltf.model.v2.MaterialModelV2;
import io.netty.buffer.ByteBuf;
import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Debugging purpose only
 */
@UtilityClass
public class OgcDebugUtil {

    private static final byte[] PRINTABLE = new byte[] {
            // 1  2  3  4  5  6  7  8  9  A  B  C  D  E  F
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, // 00
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, // 10
            1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, // 20
            1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, // 30
            1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, // 40
            1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, // 50
            1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, // 60
            1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, // 70
            1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 0, // 80
            0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, // 90
            0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, // A0
            1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, // B0
            1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, // C0
            1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, // D0
            1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, // E0
            1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, // F0
    };

    public <T extends TileData> List<T> fetchGeoCoordinateRayIntersectingData(
            TileData data, double[] coordinate, Class<T> clazz
    ) throws IOException {

        List<Tileset> tilesets = new ArrayList<>();
        List<T> dataList = new ArrayList<>();

        tilesets.add((Tileset) data);
        do {
            List<Tileset> newTilesets = new ArrayList<>();

            for(Tileset tileset : tilesets) {
                List<TileContentLink> contentLinks = getGeoCoordinateRayIntersectingContentLinks(
                        tileset, coordinate);
                List<TileData> tilesetDataList = new ArrayList<>();
                for(TileContentLink contentLink : contentLinks) {
                    tilesetDataList.add(contentLink.fetch());
                }

                for(TileData tilesetData : tilesetDataList) {
                    if(tilesetData instanceof Tileset) {
                        newTilesets.add((Tileset) tilesetData);
                    } else if(clazz.isInstance(tilesetData)) {
                        dataList.add(BtrUtil.uncheckedCast(tilesetData));
                    }
                }
            }
            tilesets = newTilesets;
        } while(!tilesets.isEmpty());

        return dataList;
    }

    public List<TileContentLink> getGeoCoordinateRayIntersectingContentLinks(Tileset tileset, double[] coordinate) {

        List<Tile> tiles = new ArrayList<>();
        List<TileContentLink> result = new ArrayList<>();

        tiles.add(tileset.getRootTile());
        do {
            List<Tile> newTiles = new ArrayList<>();
            for(Tile tile : tiles) {
                Matrix4 transform = tile.getTrueTransform();
                if(!tile.getBoundingVolume().intersectsGeoCoordinateRay(coordinate, transform))
                    continue;

                List<Tile> children = tile.getChildren();
                List<TileContentLink> tileContents = tile.getContents();

                TileRefinement refinement = tile.getRefinement();
                if(refinement != TileRefinement.REPLACE || children.isEmpty()) {
                    for(TileContentLink tileContent : tileContents) {
                        if(tileContent.getBoundingVolume() != null) {
                            boolean intersects = tileContent.getBoundingVolume()
                                    .intersectsGeoCoordinateRay(coordinate, transform);
                            if(intersects) continue;
                        }
                        result.add(tileContent);
                    }
                }
                newTiles.addAll(children);
            }
            tiles = newTiles;
        } while(!tiles.isEmpty());

        return result;
    }

    public void debugPrintOgcObject(Object object) {
        StringBuilder sb = new StringBuilder();
        writeOgcObjectString(sb, object, 0, false, true);
        Constants.LOGGER.debug(sb);
    }

    public void writeOgcObjectString(StringBuilder outerSB, Object object, int tabs, boolean startWithTabs, boolean details) {
        if(object == null) {
            lnTabs(outerSB, startWithTabs ? tabs : 0, "null");
            return;
        }

        StringBuilder sb = new StringBuilder();
        if(object instanceof List) {
            List<?> list = BtrUtil.uncheckedCast(object);
            if(list.isEmpty()) {
                sb.append("[]\n");
            } else {
                sb.append("[\n");
                for (Object item : list) {
                    writeOgcObjectString(sb, item, tabs + 1, true, details);
                }
                lnTabs(sb, tabs, "]");
            }

            lnTabs(outerSB, startWithTabs ? tabs : 0, sb.toString(), false);
            return;
        }
        else if(object instanceof Map) {
            Map<?, ?> map = BtrUtil.uncheckedCast(object);
            if(map.isEmpty()) {
                sb.append("{}\n");
            } else {
                sb.append("{\n");
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    lnTabs(sb, tabs+1, "\"" + entry.getKey().toString() + "\" -> ", entry.getValue(), details);
                }
                lnTabs(sb, tabs, "}");
            }

            lnTabs(outerSB, startWithTabs ? tabs : 0, sb.toString(), false);
            return;
        }
        else if(object.getClass().isArray()) {
            Object[] array = BtrUtil.uncheckedCast(object);
            if(array.length == 0) {
                sb.append("[]\n");
            } else {
                sb.append("[\n");
                for (Object item : array) {
                    writeOgcObjectString(sb, item, tabs + 1, true, details);
                }
                lnTabs(sb, tabs, "]");
            }

            lnTabs(outerSB, startWithTabs ? tabs : 0, sb.toString(), false);
            return;
        }

        if(!details) {
            lnTabs(outerSB, startWithTabs ? tabs : 0, objectHash(object));
            return;
        }

        sb.append(objectHash(object)).append(" {\n");
        boolean isOgcObject = false;

        if(object instanceof AccessorModel) {
            isOgcObject = true;
            AccessorModel accessorModel = (AccessorModel) object;
            lnTabs(sb, tabs+1, "byteOffset: " + accessorModel.getByteOffset());
            lnTabs(sb, tabs+1, "count: " + accessorModel.getCount());
            lnTabs(sb, tabs+1, "type: " + accessorModel.getElementType());
            lnTabs(sb, tabs+1, "dataType: " + accessorModel.getComponentDataType());
            lnTabs(sb, tabs+1, "min: " + Arrays.toString(accessorModel.getMin()));
            lnTabs(sb, tabs+1, "max: " + Arrays.toString(accessorModel.getMax()));
            lnTabs(sb, tabs+1, "bufferViewModel: ", accessorModel.getBufferViewModel(), false);
        }
        if(object instanceof BufferViewModel) {
            isOgcObject = true;
            BufferViewModel bufferViewModel = (BufferViewModel) object;
            lnTabs(sb, tabs+1, "byteOffset: " + bufferViewModel.getByteOffset());
            lnTabs(sb, tabs+1, "byteLength: " + bufferViewModel.getByteLength());
            lnTabs(sb, tabs+1, "byteStride: " + bufferViewModel.getByteStride());
            lnTabs(sb, tabs+1, "target: " + bufferViewModel.getTarget());
            lnTabs(sb, tabs+1, "bufferModel: ", bufferViewModel.getBufferModel(), false);
        }
        if(object instanceof BufferModel) {
            isOgcObject = true;
            BufferModel bufferModel = (BufferModel) object;
            lnTabs(sb, tabs+1, "byteLength: " + bufferModel.getByteLength());
            lnTabs(sb, tabs+1, "uri: " + bufferModel.getUri());
        }
        if(object instanceof GltfModel) {
            isOgcObject = true;
            GltfModel gltfModel = (GltfModel) object;
            lnTabs(sb, tabs+1, "accessorModels: ", gltfModel.getAccessorModels(), true);
            lnTabs(sb, tabs+1, "animationModels: ", gltfModel.getAnimationModels(), true);
            lnTabs(sb, tabs+1, "bufferModels: ", gltfModel.getBufferModels(), true);
            lnTabs(sb, tabs+1, "bufferViewModels: ", gltfModel.getBufferViewModels(), true);
            lnTabs(sb, tabs+1, "cameraModels: ", gltfModel.getCameraModels(), true);
            lnTabs(sb, tabs+1, "imageModels: ", gltfModel.getImageModels(), true);
            lnTabs(sb, tabs+1, "materialModels: ", gltfModel.getMaterialModels(), true);
            lnTabs(sb, tabs+1, "meshModels: ", gltfModel.getMeshModels(), true);
            lnTabs(sb, tabs+1, "nodeModels: ", gltfModel.getNodeModels(), true);
            lnTabs(sb, tabs+1, "sceneModels: ", gltfModel.getSceneModels(), true);
            lnTabs(sb, tabs+1, "skinModels: ", gltfModel.getSkinModels(), true);
            lnTabs(sb, tabs+1, "textureModels: ", gltfModel.getTextureModels(), true);
        }
        if(object instanceof ImageModel) {
            isOgcObject = true;
            ImageModel imageModel = (ImageModel) object;
            lnTabs(sb, tabs+1, "uri: " + imageModel.getUri());
            lnTabs(sb, tabs+1, "mimeType: " + imageModel.getMimeType());
            lnTabs(sb, tabs+1, "bufferViewModel: ", imageModel.getBufferViewModel(), false);
            lnTabs(sb, tabs+1, "imageData: " + imageModel.getImageData());
        }
        if(object instanceof MaterialModelV2) {
            isOgcObject = true;
            MaterialModelV2 materialModel = (MaterialModelV2) object;
            lnTabs(sb, tabs+1, "base: ", materialModel.getBaseColorTexture(), false);
            lnTabs(sb, tabs+1, "emissive: ", materialModel.getEmissiveTexture(), false);
            lnTabs(sb, tabs+1, "normal: ", materialModel.getNormalTexture(), false);
            lnTabs(sb, tabs+1, "occlusion: ", materialModel.getOcclusionTexture(), false);
            lnTabs(sb, tabs+1, "roughness: ", materialModel.getMetallicRoughnessTexture(), false);
        }
        if(object instanceof MeshModel) {
            isOgcObject = true;
            MeshModel meshModel = (MeshModel) object;
            lnTabs(sb, tabs+1, "weights: " + Arrays.toString(meshModel.getWeights()));
            lnTabs(sb, tabs+1, "primitiveModels: ", meshModel.getMeshPrimitiveModels(), true);
        }
        if(object instanceof MeshPrimitiveModel) {
            isOgcObject = true;
            MeshPrimitiveModel meshPrimitiveModel = (MeshPrimitiveModel) object;
            lnTabs(sb, tabs+1, "mode: " + meshPrimitiveModel.getMode());
            lnTabs(sb, tabs+1, "materialModel: ", meshPrimitiveModel.getMaterialModel(), false);
            lnTabs(sb, tabs+1, "indices: ", meshPrimitiveModel.getIndices(), false);
            lnTabs(sb, tabs+1, "attributes: ", meshPrimitiveModel.getAttributes(), false);
        }
        if(object instanceof NodeModel) {
            isOgcObject = true;
            NodeModel nodeModel = (NodeModel) object;
            lnTabs(sb, tabs+1, "parent: ", nodeModel.getParent(), false);
            lnTabs(sb, tabs+1, "children: ", nodeModel.getChildren(), false);
            lnTabs(sb, tabs+1, "meshModels: ", nodeModel.getMeshModels(), false);
            lnTabs(sb, tabs+1, "skinModel: ", nodeModel.getSkinModel(), false);
        }
        if(object instanceof SceneModel) {
            isOgcObject = true;
            SceneModel sceneModel = (SceneModel) object;
            lnTabs(sb, tabs+1, "nodeModels: ", sceneModel.getNodeModels(), false);
        }
        if(object instanceof TextureModel) {
            isOgcObject = true;
            TextureModel textureModel = (TextureModel) object;
            lnTabs(sb, tabs+1, "magFilter: " + textureModel.getMagFilter());
            lnTabs(sb, tabs+1, "minFilter: " + textureModel.getMinFilter());
            lnTabs(sb, tabs+1, "wrapS: " + textureModel.getWrapS());
            lnTabs(sb, tabs+1, "wrapT: " + textureModel.getWrapT());
            lnTabs(sb, tabs+1, "imageModel: ", textureModel.getImageModel(), false);
        }

        if(object instanceof NamedModelElement) {
            isOgcObject = true;
            NamedModelElement namedModelElement = (NamedModelElement) object;
            if(namedModelElement.getName() != null) {
                lnTabs(sb, tabs + 1, "name: " + namedModelElement.getName());
            }
        }
        if(object instanceof ModelElement) {
            isOgcObject = true;
            ModelElement modelElement = (ModelElement) object;
            if(modelElement.getExtensions() != null) {
                lnTabs(sb, tabs + 1, "extensions: ", modelElement.getExtensions(), true);
            }
            if(modelElement.getExtras() != null) {
                lnTabs(sb, tabs + 1, "extras: ", modelElement.getExtras(), true);
            }
        }

        if(!isOgcObject) {
            lnTabs(outerSB, startWithTabs ? tabs : 0, object.toString());
            return;
        }

        lnTabs(sb, tabs, "}");
        lnTabs(outerSB, startWithTabs ? tabs : 0, sb.toString(), false);
    }

    private static String objectHash(Object object) {
        return object.getClass().getSimpleName() +
                "(hash=" + String.format("%08x", object.hashCode()) + ")";
    }

    private static void lnTabs(StringBuilder sb, int tabs, String s) {
        lnTabs(sb, tabs, s, true);
    }

    private static void lnTabs(StringBuilder sb, int tabs, String s, boolean newLine) {
        for(int i = 0; i < tabs; i++) sb.append("  ");
        sb.append(s);
        if(newLine) sb.append("\n");
    }

    private static void lnTabs(StringBuilder sb, int tabs, String s, Object o, boolean details) {
        for(int i = 0; i < tabs; i++) sb.append("  ");
        sb.append(s);
        writeOgcObjectString(sb, o, tabs, false, details);
    }

    public void writeBinaryTableString(StringBuilder sb, ByteBuf buf, int width, int limit) {

        // table head
        sb.append("|          | ");
        for(int i = 0; i < width; i++) {
            sb.append(String.format("%02x ", i));
        }
        sb.append("|\n");

        // hl
        sb.append("+----------+-");
        for(int i = 0; i < width; i++) {
            sb.append("---");
        }
        sb.append("+-");
        for(int i = 0; i < width; i++) {
            sb.append("-");
        }
        sb.append("\n");

        // table body
        byte[] row = new byte[width];
        for (int i = 0, c = 0; i < (limit / width) * width; i++) {
            if (i % width == 0) {
                sb.append(String.format("| %08x | ", i));
            }

            if(i < limit && buf.readableBytes() != 0) {
                byte data = buf.readByte();
                row[c++] = data;
                sb.append(String.format("%02x ", data));
            } else {
                sb.append("   ");
            }

            if (i % width == width - 1) {
                sb.append("| ");
                for(int j = 0; j < c; j++) {
                    int unsigned = Byte.toUnsignedInt(row[j]);
                    if(PRINTABLE[unsigned] == 0) sb.append(".");
                    else sb.append(String.format("%c", unsigned));
                }
                c = 0;
                sb.append("\n");
            }
        }
    }
}
