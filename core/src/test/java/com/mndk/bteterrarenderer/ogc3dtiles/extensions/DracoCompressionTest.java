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
    private GltfModel readGltfModel(String testFileName, SpheroidCoordinatesConverter converter) throws IOException {
        try(InputStream stream = getClass().getClassLoader().getResourceAsStream(testFileName)) {
            if (stream == null) throw new IOException(testFileName + " not found");

            TileData data = TileResourceManager.parse(stream, converter);
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
        SpheroidCoordinatesConverter converter = SpheroidCoordinatesConverter.WGS84;
        String[] files = {
                "glb_models/model_test1.glb",
                "glb_models/model_test2.glb",
                "glb_models/model_test3.glb",
                "glb_models/model_test4.glb"
        };
        for(String file : files) {
            GltfModel model = this.readGltfModel(file, converter);
            Pair<Mesh, DracoMeshCompression> pair = this.decodeDracoMeshData(model);
            Mesh mesh = pair.getLeft();
            // Temporary assertion to suppress variable unused warning
            Assert.assertTrue(mesh.getNumPoints() >= 0);
        }
    }

//    @Test
//    public void givenModelTestGlbFile_testDecode() throws IOException, OutOfProjectionBoundsException {
//        SpheroidCoordinatesConverter converter = SpheroidCoordinatesConverter.WGS84;
//        GltfModel model = this.readGltfModel("glb_models/model_test3.glb", converter);
//
//        float[] posArrayTemp = new float[3];
//        float[] translation = model.getNodeModels().get(0).getTranslation();
//        double[] avgArray = new double[] {0, 0, 0};
//
//        Pair<Mesh, DracoMeshCompression> pair = this.decodeDracoMeshData(model);
//        Mesh mesh = pair.getLeft();
//        DracoMeshCompression draco = pair.getRight();
//        PointAttribute att = mesh.getAttribute(draco.getAttributes().get("POSITION"));
//        for(PointIndex p : PointIndex.range(0, mesh.getNumPoints())) {
//            att.getMappedValue(p, Pointer.wrap(posArrayTemp));
//            double pos0 = (double) posArrayTemp[0] + translation[0];
//            double pos1 = -((double) posArrayTemp[2] + translation[2]);
//            double pos2 = (double) posArrayTemp[1] + translation[1];
//            avgArray[0] += pos0;
//            avgArray[1] += pos1;
//            avgArray[2] += pos2;
//
//            Cartesian3 cartesian3 = new Cartesian3(pos0, pos1, pos2);
//            Spheroid3 spheroid3 = converter.toSpheroid(cartesian3);
//            double[] gamePos = Projections.BTE.fromGeo(spheroid3.getLongitudeDegrees(), spheroid3.getLatitudeDegrees());
//
//            int gameX = (int) Math.round(gamePos[0]);
//            int gameY = (int) Math.round(spheroid3.getHeight());
//            int gameZ = (int) Math.round(gamePos[1]);
//            System.out.println("setblock " + gameX + " " + gameY + " " + gameZ + " minecraft:stone");
//        }
//        avgArray[0] /= mesh.getNumPoints();
//        avgArray[1] /= mesh.getNumPoints();
//        avgArray[2] /= mesh.getNumPoints();
//
//        Cartesian3 average = new Cartesian3(avgArray[0], avgArray[1], avgArray[2]);
//        Spheroid3 expected = Spheroid3.fromDegrees(-74.00497460764946, 40.71228662124228, 20);
//        System.out.println("Average: " + average + " -> " + converter.toSpheroid(average));
//        System.out.println("Expected: " + converter.toCartesian(expected) + " -> " + expected);
//    }
}
