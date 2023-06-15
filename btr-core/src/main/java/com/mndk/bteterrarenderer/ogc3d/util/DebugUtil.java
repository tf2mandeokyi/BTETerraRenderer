package com.mndk.bteterrarenderer.ogc3d.util;

import com.mndk.bteterrarenderer.ogc3d.tile.Tile;
import com.mndk.bteterrarenderer.ogc3d.tile.TileContent;
import com.mndk.bteterrarenderer.util.BtrUtil;
import de.javagl.jgltf.model.*;
import de.javagl.jgltf.model.v2.MaterialModelV2;

import java.io.PrintStream;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Debugging purpose only
 */
public class DebugUtil {

    public static List<TileContent> fetchCoordinateContainingTiles(Tile tile, double[] geoCoordinate, int depth) {
        for (int i = 0; i < depth; i++) System.out.print("  ");
        System.out.println("error = " + tile.getGeometricError());

        List<TileContent> tileContents = tile.getContents(), result = new ArrayList<>();
        if(tileContents.size() != 0) {
            result.addAll(tileContents);
            for (TileContent content : tileContents) {
                for (int i = 0; i < depth; i++) System.out.print("  ");
                System.out.println(content);
            }
        } else {
            for (int i = 0; i < depth; i++) System.out.print("  ");
            System.out.println("(empty)");
        }

        List<Tile> children = tile.getChildren();
        if(children.size() != 0) {
            for (int j = 0; j < children.size(); j++) {
                Tile child = children.get(j);
                if (!child.getBoundingVolume().intersectsGeoCoordinate(geoCoordinate)) continue;
                for (int i = 0; i < depth; i++) System.out.print("  ");
                System.out.println("[" + j +"]:");
                result.addAll(fetchCoordinateContainingTiles(child, geoCoordinate, depth + 1));
            }
        }
        return result;
    }

    public static void printOgcObject(StringBuilder outerSB, Object object, int tabs, boolean startWithTabs) {
        if(object == null) {
            lnTabs(outerSB, startWithTabs ? tabs : 0, "null");
            return;
        }

        StringBuilder sb = new StringBuilder();
        if(object instanceof List) {
            List<?> list = BtrUtil.uncheckedCast(object);
            if(list.size() == 0) {
                sb.append("[]\n");
            } else {
                sb.append("[\n");
                for (Object item : list) {
                    printOgcObject(sb, item, tabs + 1, true);
                }
                lnTabs(sb, tabs, "]");
            }

            lnTabs(outerSB, startWithTabs ? tabs : 0, sb.toString(), false);
            return;
        }
        else if(object instanceof Map) {
            Map<?, ?> map = BtrUtil.uncheckedCast(object);
            if(map.size() == 0) {
                sb.append("{}\n");
            } else {
                sb.append("{\n");
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    lnTabs(sb, tabs+1, "\"" + entry.getKey().toString() + "\" -> ", entry.getValue());
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
                    printOgcObject(sb, item, tabs + 1, true);
                }
                lnTabs(sb, tabs, "]");
            }

            lnTabs(outerSB, startWithTabs ? tabs : 0, sb.toString(), false);
            return;
        }

        sb.append("{\n");
        lnTabs(sb, tabs+1, "this: " + object);

        if(object instanceof NodeModel) {
            NodeModel nodeModel = (NodeModel) object;
            lnTabs(sb, tabs+1, "parent: " + nodeModel.getParent());
            lnTabs(sb, tabs+1, "children: " + nodeModel.getChildren());
            lnTabs(sb, tabs+1, "meshModels: ", nodeModel.getMeshModels());
            lnTabs(sb, tabs+1, "skinModel: ", nodeModel.getSkinModel());
        }
        else if(object instanceof MeshModel) {
            MeshModel meshModel = (MeshModel) object;
            lnTabs(sb, tabs+1, "weights: " + Arrays.toString(meshModel.getWeights()));
            lnTabs(sb, tabs+1, "primitiveModels: ", meshModel.getMeshPrimitiveModels());
        }
        else if(object instanceof MeshPrimitiveModel) {
            MeshPrimitiveModel meshPrimitiveModel = (MeshPrimitiveModel) object;
            lnTabs(sb, tabs+1, "mode: " + meshPrimitiveModel.getMode());
            lnTabs(sb, tabs+1, "materialModel: ", meshPrimitiveModel.getMaterialModel());
            lnTabs(sb, tabs+1, "accessorModel: ", meshPrimitiveModel.getIndices());
            lnTabs(sb, tabs+1, "attributes: ", meshPrimitiveModel.getAttributes());
        }
        else if(object instanceof MaterialModelV2) {
            MaterialModelV2 materialModel = (MaterialModelV2) object;
            lnTabs(sb, tabs+1, "base: ", materialModel.getBaseColorTexture());
            lnTabs(sb, tabs+1, "emissive: ", materialModel.getEmissiveTexture());
            lnTabs(sb, tabs+1, "normal: ", materialModel.getNormalTexture());
            lnTabs(sb, tabs+1, "occlusion: ", materialModel.getOcclusionTexture());
            lnTabs(sb, tabs+1, "roughness: ", materialModel.getMetallicRoughnessTexture());
        }
        else if(object instanceof AccessorModel) {
            AccessorModel accessorModel = (AccessorModel) object;
            lnTabs(sb, tabs+1, "byteOffset: " + accessorModel.getByteOffset());
            lnTabs(sb, tabs+1, "count: " + accessorModel.getCount());
            lnTabs(sb, tabs+1, "type: " + accessorModel.getElementType());
            lnTabs(sb, tabs+1, "dataType: " + accessorModel.getComponentDataType());
            lnTabs(sb, tabs+1, "min: " + Arrays.toString(accessorModel.getMin()));
            lnTabs(sb, tabs+1, "max: " + Arrays.toString(accessorModel.getMax()));
            lnTabs(sb, tabs+1, "bufferViewModel: ", accessorModel.getBufferViewModel());
        }
        else if(object instanceof BufferViewModel) {
            BufferViewModel bufferViewModel = (BufferViewModel) object;
            lnTabs(sb, tabs+1, "byteOffset: " + bufferViewModel.getByteOffset());
            lnTabs(sb, tabs+1, "byteLength: " + bufferViewModel.getByteLength());
            lnTabs(sb, tabs+1, "byteStride: " + bufferViewModel.getByteStride());
            lnTabs(sb, tabs+1, "target: " + bufferViewModel.getTarget());
            lnTabs(sb, tabs+1, "bufferModel: ", bufferViewModel.getBufferModel());
        }
        else if(object instanceof BufferModel) {
            BufferModel bufferModel = (BufferModel) object;
            lnTabs(sb, tabs+1, "byteLength: " + bufferModel.getByteLength());
            lnTabs(sb, tabs+1, "uri: " + bufferModel.getUri());
        }
        else {
            lnTabs(outerSB, startWithTabs ? tabs : 0, object.toString());
            return;
        }

        lnTabs(sb, tabs, "}");
        lnTabs(outerSB, startWithTabs ? tabs : 0, sb.toString(), false);
    }

    private static void lnTabs(StringBuilder sb, int tabs, String s) {
        lnTabs(sb, tabs, s, true);
    }

    private static void lnTabs(StringBuilder sb, int tabs, String s, boolean newLine) {
        for(int i = 0; i < tabs; i++) sb.append("  ");
        sb.append(s);
        if(newLine) sb.append("\n");
    }

    private static void lnTabs(StringBuilder sb, int tabs, String s, Object o) {
        for(int i = 0; i < tabs; i++) sb.append("  ");
        sb.append(s);
        printOgcObject(sb, o, tabs, false);
    }

    public static void printBinary(PrintStream out, ByteBuffer buffer, int width, int limit) {
        // table head
        out.print("         | ");
        for(int i = 0; i < width; i++) {
            out.printf("%02x ", i);
        }
        out.println();

        // hl
        out.print("---------+-");
        for(int i = 0; i < width; i++) {
            out.print("---");
        }
        out.println();

        // table body
        try {
            for (int i = 0; i < limit; i++) {
                byte data = buffer.get();
                if (i % width == 0) {
                    out.printf("%08x | ", i);
                }
                out.printf("%02x ", data);
                if (i % width == width - 1 || i == limit - 1) {
                    out.println();
                }
            }
        } catch(BufferUnderflowException ignored) {}
    }
}
