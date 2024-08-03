package com.mndk.bteterrarenderer.ogc3dtiles.extensions;

import com.mndk.bteterrarenderer.draco.compression.DracoDecoder;
import com.mndk.bteterrarenderer.draco.core.DecoderBuffer;
import com.mndk.bteterrarenderer.draco.core.StatusAssert;
import com.mndk.bteterrarenderer.draco.core.StatusOr;
import com.mndk.bteterrarenderer.draco.mesh.Mesh;
import com.mndk.bteterrarenderer.ogc3dtiles.TileData;
import com.mndk.bteterrarenderer.ogc3dtiles.TileResourceManager;
import com.mndk.bteterrarenderer.ogc3dtiles.gltf.extensions.DracoMeshCompression;
import com.mndk.bteterrarenderer.ogc3dtiles.gltf.extensions.GltfExtensionsUtil;
import de.javagl.jgltf.model.GltfModel;
import de.javagl.jgltf.model.MeshModel;
import de.javagl.jgltf.model.MeshPrimitiveModel;
import org.junit.Test;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class DracoCompressionTest {

    @Nonnull
    private GltfModel readGltfModel(String testFileName) throws IOException {
        try(InputStream stream = getClass().getClassLoader().getResourceAsStream(testFileName)) {
            if (stream == null) throw new IOException(testFileName + " not found");

            TileData data = TileResourceManager.parse(stream);
            GltfModel model = data.getGltfModelInstance();
            if(model == null) throw new IOException("model is null");
            return model;
        }
    }

    private DecoderBuffer getDecoderBuffer(GltfModel model) throws IOException {
        MeshModel meshModel = model.getMeshModels().get(0);
        MeshPrimitiveModel meshPrimitiveModel = meshModel.getMeshPrimitiveModels().get(0);
        DracoMeshCompression draco = GltfExtensionsUtil.getExtension(meshPrimitiveModel, DracoMeshCompression.class);
        if(draco == null) throw new IOException("DracoMeshCompression is null");

        ByteBuffer byteBuffer = model.getBufferViewModels()
                .get(draco.getBufferView())
                .getBufferViewData();
        DecoderBuffer decoderBuffer = new DecoderBuffer();
        decoderBuffer.init(byteBuffer);
        return decoderBuffer;
    }

    @Test
    public void givenModelTestGlbFile_testDecode() throws IOException {
        GltfModel model = this.readGltfModel("draco/model_test.glb");
        DecoderBuffer decoderBuffer = this.getDecoderBuffer(model);

        DracoDecoder decoder = new DracoDecoder();
        StatusOr<Mesh> meshOrError = decoder.decodeMeshFromBuffer(decoderBuffer);
        StatusAssert.assertOk(meshOrError);
    }
}
