package com.mndk.bteterrarenderer.ogc3d.format.b3dm;

import com.mndk.bteterrarenderer.BTETerraRendererConstants;
import com.mndk.bteterrarenderer.ogc3d.format.table.BatchTable;
import com.mndk.bteterrarenderer.util.IOUtil;
import de.javagl.jgltf.model.GltfModel;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Getter
@Builder
@ToString
public class Batched3DModel {

    private final int version;
    private final B3dmFeatureTable featureTable;
    private final BatchTable batchTable;
    private final GltfModel gltfModel;


    public static Batched3DModel from(InputStream input) throws IOException {
        ByteBuf buf = Unpooled.wrappedBuffer(IOUtil.readAllBytes(input));

        String magic = buf.readBytes(4).toString(StandardCharsets.UTF_8);
        if(!"b3dm".equals(magic)) throw new IOException("expected b3dm format, found: " + magic);

        int version = buf.readIntLE();
        /* int byteLength = */ buf.readIntLE();
        int featureTableJSONByteLength = buf.readIntLE();
        int featureTableBinaryByteLength = buf.readIntLE();
        int batchTableJSONByteLength = buf.readIntLE();
        int batchTableBinaryByteLength = buf.readIntLE();

        String featureTableJson = buf.readBytes(featureTableJSONByteLength).toString(StandardCharsets.UTF_8);
        byte[] featureTableBinary = buf.readBytes(featureTableBinaryByteLength).array();
        B3dmFeatureTable featureTable = B3dmFeatureTable.from(featureTableJson, featureTableBinary);

        int batchModelCount = featureTable.getBatchLength();
        String batchTableJson = buf.readBytes(batchTableJSONByteLength).toString(StandardCharsets.UTF_8);
        byte[] batchTableBinary = buf.readBytes(batchTableBinaryByteLength).array();
        BatchTable batchTable = BatchTable.from(batchModelCount, batchTableJson, batchTableBinary);

        InputStream gltfInputStream = new ByteBufInputStream(buf);
        GltfModel gltfModel = BTETerraRendererConstants.GLTF_MODEL_READER.readWithoutReferences(gltfInputStream);

        gltfInputStream.close();

        return builder()
                .version(version)
                .featureTable(featureTable)
                .batchTable(batchTable)
                .gltfModel(gltfModel)
                .build();
    }
}
