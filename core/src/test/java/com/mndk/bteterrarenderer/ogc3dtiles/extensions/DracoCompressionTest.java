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

//    // This code is just for the visualization test! Keep this disabled, or it will make the logs dirty.
//    @Test
//    public void givenModelTestGlbFile_testDecode() throws IOException {
//        SpheroidCoordinatesConverter converter = SpheroidCoordinatesConverter.WGS84;
//        GltfModel model = this.readGltfModel("glb_models/model_test1.glb", converter);
//
//        Float[] center = Arrays.stream(new String[] { "3258869.7866948475", "-1158451.3178201506", "-4877243.302543961" })
//                .map(Float::parseFloat).toArray(Float[]::new);
//        float[] posArrayTemp = new float[3];
//        float[] min = new float[] {Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE};
//        float[] max = new float[] {Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE};
//
//        List<Pair<Mesh, DracoMeshCompression>> pairs = this.decodeDracoMeshData(model);
//        for (int i = 0; i < pairs.size(); i++) {
//            Pair<Mesh, DracoMeshCompression> pair = pairs.get(i);
//            System.out.println("####### MESH NUMBER " + i + " #######");
//            Mesh mesh = pair.getLeft();
//            DracoMeshCompression draco = pair.getRight();
//            PointAttribute att = mesh.getAttribute(draco.getAttributes().get("POSITION"));
//            for (FaceIndex f : FaceIndex.range(0, mesh.getNumFaces())) {
//                for (int p = 0; p < 3; p++) {
//                    att.getMappedValue(mesh.getFace(f).get(p), Pointer.wrap(posArrayTemp));
//                    min[0] = Math.min(min[0], posArrayTemp[0]);
//                    min[1] = Math.min(min[1], posArrayTemp[1]);
//                    min[2] = Math.min(min[2], posArrayTemp[2]);
//                    max[0] = Math.max(max[0], posArrayTemp[0]);
//                    max[1] = Math.max(max[1], posArrayTemp[1]);
//                    max[2] = Math.max(max[2], posArrayTemp[2]);
//                    float x = center[0] + posArrayTemp[0];
//                    float y = center[1] + posArrayTemp[1];
//                    float z = center[2] + posArrayTemp[2];
//                    System.out.printf("%.2f, %.2f, %.2f\n", x, y, z);
//                }
//            }
//        }
//        System.out.println("min: " + min[0] + ", " + min[1] + ", " + min[2]);
//        System.out.println("max: " + max[0] + ", " + max[1] + ", " + max[2]);
//    }
//
//    @Test
//    public void sdafjklhfjkldsahjfldsa() throws IOException {
//        SpheroidCoordinatesConverter coordConverter = new SpheroidCoordinatesConverter(
//                Wgs84Constants.SEMI_MAJOR_AXIS, Wgs84Constants.SEMI_MINOR_AXIS, GeoidHeightFunction.EGM96_WW15MGH);
//        GltfModel model = this.readGltfModel("glb_models/model_test3.glb", coordConverter);
//
//        List<PreBakedModel> models = GltfModelConverter.convertModel(model, Matrix4f.IDENTITY, Projections.BTE,
//                coordConverter, true);
//        for(PreBakedModel m : models) {
//            List<GraphicsTriangle<PosTexNorm>> triangles = m.getShapes().getShapesForFormat(DrawingFormat.TRI_PTN_ALPHA);
//            for(GraphicsTriangle<PosTexNorm> triangle : triangles) {
//                // Only print the positions
//                for(int i = 0; i < 3; i++) {
//                    PosTexNorm vertex = triangle.getVertex(i);
//                    System.out.printf("%.2f, %.2f, %.2f\n", vertex.px, vertex.py, vertex.pz);
//                }
//            }
//        }
//    }

}
