package com.mndk.bteterrarenderer.ogc3dtiles.extensions;

import com.mndk.bteterrarenderer.draco.compression.DracoDecoder;
import com.mndk.bteterrarenderer.draco.core.DecoderBuffer;
import com.mndk.bteterrarenderer.draco.core.StatusAssert;
import com.mndk.bteterrarenderer.draco.mesh.Mesh;
import com.mndk.bteterrarenderer.ogc3dtiles.TileData;
import com.mndk.bteterrarenderer.ogc3dtiles.TileResourceManager;
import com.mndk.bteterrarenderer.ogc3dtiles.gltf.extensions.DracoMeshCompression;
import com.mndk.bteterrarenderer.ogc3dtiles.gltf.extensions.GltfExtensionsUtil;
import de.javagl.jgltf.model.GltfModel;
import de.javagl.jgltf.model.MeshModel;
import de.javagl.jgltf.model.MeshPrimitiveModel;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
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

    private Pair<Mesh, DracoMeshCompression> decodeDracoMeshData(GltfModel model) throws IOException {
        MeshModel meshModel = model.getMeshModels().get(0);
        MeshPrimitiveModel meshPrimitiveModel = meshModel.getMeshPrimitiveModels().get(0);
        DracoMeshCompression draco = GltfExtensionsUtil.getExtension(meshPrimitiveModel, DracoMeshCompression.class);
        if(draco == null) throw new IOException("DracoMeshCompression is null");

        ByteBuffer byteBuffer = model.getBufferViewModels()
                .get(draco.getBufferView())
                .getBufferViewData();
        DecoderBuffer decoderBuffer = new DecoderBuffer();
        decoderBuffer.init(byteBuffer);

        DracoDecoder decoder = new DracoDecoder();
        return Pair.of(StatusAssert.assertOk(decoder.decodeMeshFromBuffer(decoderBuffer)), draco);
    }

    @Test
    public void givenModelTestGlbFiles_testDecode() throws IOException {
        String[] files = { "glb_models/model_test1.glb", "glb_models/model_test2.glb" };
        for(String file : files) {
            GltfModel model = this.readGltfModel(file);
            Pair<Mesh, DracoMeshCompression> pair = this.decodeDracoMeshData(model);
            Mesh mesh = pair.getLeft();
            // Temporary assertion to suppress variable unused warning
            Assert.assertTrue(mesh.getNumPoints() >= 0);
        }
    }

//    @Test
//    public void givenModelTestGlbFile_testDecode() throws IOException {
//        GltfModel model = this.readGltfModel("glb_models/model_test1.glb");
//
//        float[] pos = new float[3];
//        float[] translation = model.getNodeModels().get(0).getTranslation();
//        float[] average = new float[] {0, 0, 0};
//
//        Pair<Mesh, DracoMeshCompression> pair = this.decodeDracoMeshData(model);
//        Mesh mesh = pair.getLeft();
//        DracoMeshCompression draco = pair.getRight();
//        PointAttribute att = mesh.getAttribute(draco.getAttributes().get("POSITION"));
//        for(PointIndex p : PointIndex.range(0, mesh.getNumPoints())) {
//            att.getMappedValue(p, Pointer.wrap(pos));
//            pos[0] += translation[0];
//            pos[1] += translation[1];
//            pos[2] += translation[2];
//            average[0] += pos[0];
//            average[1] += pos[1];
//            average[2] += pos[2];
//            Cartesian3 cartesian3 = new Cartesian3(pos);
//            Spheroid3 spheroid3 = cartesian3.toSpheroidalCoordinate();
//            System.out.println("Point " + p + ": " + cartesian3 + " -> " + spheroid3);
//        }
//        average[0] /= mesh.getNumPoints();
//        average[1] /= mesh.getNumPoints();
//        average[2] /= mesh.getNumPoints();
//        System.out.println("Average: " + new Cartesian3(average));
//        System.out.println("Expected: " + new Spheroid3(56.043217666000245, -11.996803611025177, 0)
//                .toCartesianCoordinate());
//    }
}
