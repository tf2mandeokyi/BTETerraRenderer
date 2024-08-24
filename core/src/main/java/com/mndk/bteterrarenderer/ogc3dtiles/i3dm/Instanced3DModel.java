package com.mndk.bteterrarenderer.ogc3dtiles.i3dm;

import com.mndk.bteterrarenderer.core.util.IOUtil;
import com.mndk.bteterrarenderer.ogc3dtiles.TileData;
import com.mndk.bteterrarenderer.ogc3dtiles.TileDataFormat;
import com.mndk.bteterrarenderer.ogc3dtiles.gltf.TileGltfModel;
import com.mndk.bteterrarenderer.ogc3dtiles.math.SpheroidCoordinatesConverter;
import com.mndk.bteterrarenderer.ogc3dtiles.table.BatchTable;
import de.javagl.jgltf.model.GltfModel;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.ToString;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Getter
@ToString
public class Instanced3DModel extends TileData {

    private final int version;
    private final I3dmFeatureTable featureTable;
    private final BatchTable batchTable;
    private final int gltfFormat;
    private final TileGltfModel gltfModel;

    private Instanced3DModel(int version,
                             I3dmFeatureTable featureTable, BatchTable batchTable,
                             int gltfFormat, TileGltfModel gltfModel) {
        super(TileDataFormat.I3DM);
        this.version = version;
        this.featureTable = featureTable;
        this.batchTable = batchTable;
        this.gltfFormat = gltfFormat;
        this.gltfModel = gltfModel;
    }

    public static Instanced3DModel from(ByteBuf buf, SpheroidCoordinatesConverter converter) throws IOException {
        String magic = buf.readBytes(4).toString(StandardCharsets.UTF_8);
        if(!"i3dm".equals(magic)) throw new IOException("expected i3dm format, found: " + magic);

        int version = buf.readIntLE();
        /* int byteLength = */ buf.readIntLE();
        int featureTableJSONByteLength = buf.readIntLE();
        int featureTableBinaryByteLength = buf.readIntLE();
        int batchTableJSONByteLength = buf.readIntLE();
        int batchTableBinaryByteLength = buf.readIntLE();
        int gltfFormat = buf.readIntLE();

        String featureTableJson = buf.readBytes(featureTableJSONByteLength).toString(StandardCharsets.UTF_8);
        byte[] featureTableBinary = IOUtil.readAllBytes(buf.readBytes(featureTableBinaryByteLength));
        I3dmFeatureTable featureTable = I3dmFeatureTable.from(featureTableJson, featureTableBinary, converter);

        int batchModelCount = featureTable.getInstances().length;
        String batchTableJson = buf.readBytes(batchTableJSONByteLength).toString(StandardCharsets.UTF_8);
        byte[] batchTableBinary = IOUtil.readAllBytes(buf.readBytes(batchTableBinaryByteLength));
        BatchTable batchTable = BatchTable.from(batchModelCount, batchTableJson, batchTableBinary);

        TileGltfModel gltfModel = TileGltfModel.from(buf);
        return new Instanced3DModel(version, featureTable, batchTable, gltfFormat, gltfModel);
    }

    @Nullable
    @Override
    public GltfModel getGltfModelInstance() {
        return this.getGltfModel().getInstance();
    }
}
