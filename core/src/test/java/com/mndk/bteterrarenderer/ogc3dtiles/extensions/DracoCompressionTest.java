package com.mndk.bteterrarenderer.ogc3dtiles.extensions;

import com.mndk.bteterrarenderer.draco.compression.DracoDecoder;
import com.mndk.bteterrarenderer.draco.core.DecoderBuffer;
import com.mndk.bteterrarenderer.draco.core.StatusAssert;
import com.mndk.bteterrarenderer.draco.mesh.Mesh;
import com.mndk.bteterrarenderer.ogc3dtiles.TileData;
import com.mndk.bteterrarenderer.ogc3dtiles.TileResourceManager;
import com.mndk.bteterrarenderer.ogc3dtiles.gltf.extensions.DracoMeshCompression;
import com.mndk.bteterrarenderer.ogc3dtiles.gltf.extensions.GltfExtensionsUtil;
import com.mndk.bteterrarenderer.ogc3dtiles.math.SpheroidCoordinatesConverter;
import de.javagl.jgltf.model.BufferViewModel;
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
import java.util.ArrayList;
import java.util.List;

public class DracoCompressionTest {

    @Nonnull
    private GltfModel readGltfModel(String testFileName, SpheroidCoordinatesConverter converter) throws IOException {
        try(InputStream stream = getClass().getClassLoader().getResourceAsStream(testFileName)) {
            if (stream == null) throw new IOException(testFileName + " not found");

            TileData data = TileResourceManager.parse(stream, converter);
            GltfModel model = data.getGltfModelInstance();
            if(model == null) throw new IOException("model is null");
            return model;
        }
    }

    private Pair<Mesh, DracoMeshCompression> decodeDracoMeshData(MeshPrimitiveModel meshPrimitiveModel,
                                                                 List<BufferViewModel> bufferViewModels) throws IOException {
        DracoMeshCompression draco = GltfExtensionsUtil.getExtension(meshPrimitiveModel, DracoMeshCompression.class);
        if(draco == null) throw new IOException("DracoMeshCompression is null");

        ByteBuffer byteBuffer = bufferViewModels.get(draco.getBufferView()).getBufferViewData();
        DecoderBuffer decoderBuffer = new DecoderBuffer();
        decoderBuffer.init(byteBuffer);

        DracoDecoder decoder = new DracoDecoder();
        return Pair.of(StatusAssert.assertOk(decoder.decodeMeshFromBuffer(decoderBuffer)), draco);
    }

    private List<Pair<Mesh, DracoMeshCompression>> decodeDracoMeshData(GltfModel model) throws IOException {
        List<Pair<Mesh, DracoMeshCompression>> meshes = new ArrayList<>();
        List<BufferViewModel> bufferViewModels = model.getBufferViewModels();
        for(MeshModel meshModel : model.getMeshModels()) {
            for(MeshPrimitiveModel primitiveModel : meshModel.getMeshPrimitiveModels()) {
                meshes.add(decodeDracoMeshData(primitiveModel, bufferViewModels));
            }
        }
        return meshes;
    }

    @Test
    public void givenModelTestGlbFiles_testDecode() throws IOException {
        SpheroidCoordinatesConverter converter = SpheroidCoordinatesConverter.WGS84;
        String[] files = {
                "glb_models/model_test1.glb",
                "glb_models/model_test2.glb",
                "glb_models/model_test3.glb",
                "glb_models/model_test4.glb"
        };
        for(String file : files) {
            GltfModel model = this.readGltfModel(file, converter);
            List<Pair<Mesh, DracoMeshCompression>> pairs = this.decodeDracoMeshData(model);
            for(Pair<Mesh, DracoMeshCompression> pair : pairs) {
                Mesh mesh = pair.getLeft();
                Assert.assertTrue(mesh.getNumPoints() >= 0);
            }
        }
    }

//    // This code is just for the visualization test! Keep this disabled or it will make the logs dirty.
//    @Test
//    public void givenModelTestGlbFile_testDecode() throws IOException {
//        SpheroidCoordinatesConverter converter = SpheroidCoordinatesConverter.WGS84;
//        GltfModel model = this.readGltfModel("glb_models/model_test1.glb", converter);
//
//        float[] texArrayTemp = new float[2];
//
//        List<Pair<Mesh, DracoMeshCompression>> pairs = this.decodeDracoMeshData(model);
//        for (int i = 0; i < pairs.size(); i++) {
//            Pair<Mesh, DracoMeshCompression> pair = pairs.get(i);
//            System.out.println("####### MESH NUMBER " + i + " #######");
//            Mesh mesh = pair.getLeft();
//            DracoMeshCompression draco = pair.getRight();
//            PointAttribute att = mesh.getAttribute(draco.getAttributes().get("TEXCOORD_0"));
//            for (FaceIndex f : FaceIndex.range(0, mesh.getNumFaces())) {
//                System.out.print("\\operatorname{polygon}\\left(");
//                for (int p = 0; p < 3; p++) {
//                    att.getMappedValue(mesh.getFace(f).get(p), Pointer.wrap(texArrayTemp));
//                    float u = texArrayTemp[0];
//                    float v = texArrayTemp[1];
//                    System.out.printf("\\left(%.5f,%.5f\\right)", u, v);
//                    if (p < 2) System.out.print(",");
//                }
//                System.out.println("\\right)");
//            }
//        }
//    }
}
