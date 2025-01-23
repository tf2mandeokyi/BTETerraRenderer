package com.mndk.bteterrarenderer.ogc3dtiles.b3dm;

import com.mndk.bteterrarenderer.ogc3dtiles.TileData;
import com.mndk.bteterrarenderer.ogc3dtiles.TileDataFormat;
import com.mndk.bteterrarenderer.ogc3dtiles.gltf.TileGltfModel;
import com.mndk.bteterrarenderer.ogc3dtiles.table.BatchTable;
import com.mndk.bteterrarenderer.util.IOUtil;
import de.javagl.jgltf.model.GltfModel;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.ToString;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Getter
@ToString
public class Batched3DModel extends TileData {

    private final int version;
    private final B3dmFeatureTable featureTable;
    private final BatchTable batchTable;
    private final TileGltfModel gltfModel;

    private Batched3DModel(int version,
                           B3dmFeatureTable featureTable, BatchTable batchTable,
                           TileGltfModel gltfModel) {
        super(TileDataFormat.B3DM);
        this.version = version;
        this.featureTable = featureTable;
        this.batchTable = batchTable;
        this.gltfModel = gltfModel;
    }

    public static Batched3DModel from(ByteBuf buf) throws IOException {
        String magic = buf.readBytes(4).toString(StandardCharsets.UTF_8);
        if (!"b3dm".equals(magic)) throw new IOException("expected b3dm format, found: " + magic);

        int version = buf.readIntLE();
        /* int byteLength = */ buf.readIntLE();
        int featureTableJSONByteLength = buf.readIntLE();
        int featureTableBinaryByteLength = buf.readIntLE();
        int batchTableJSONByteLength = buf.readIntLE();
        int batchTableBinaryByteLength = buf.readIntLE();

        String featureTableJson = buf.readBytes(featureTableJSONByteLength).toString(StandardCharsets.UTF_8);
        byte[] featureTableBinary = IOUtil.readAllBytes(buf.readBytes(featureTableBinaryByteLength));
        B3dmFeatureTable featureTable = B3dmFeatureTable.from(featureTableJson, featureTableBinary);

        int batchModelCount = featureTable.getBatchLength();
        String batchTableJson = buf.readBytes(batchTableJSONByteLength).toString(StandardCharsets.UTF_8);
        byte[] batchTableBinary = IOUtil.readAllBytes(buf.readBytes(batchTableBinaryByteLength));
        BatchTable batchTable = batchTableBinaryByteLength == 0 ?
                BatchTable.empty() :
                BatchTable.from(batchModelCount, batchTableJson, batchTableBinary);

        TileGltfModel gltfModel = TileGltfModel.from(buf);
        return new Batched3DModel(version, featureTable, batchTable, gltfModel);
    }

    @Nullable
    @Override
    public String getCopyright() {
        return this.gltfModel.getCopyright();
    }

    @Nullable
    @Override
    public GltfModel getGltfModelInstance() {
        return this.gltfModel.getInstance();
    }
}
