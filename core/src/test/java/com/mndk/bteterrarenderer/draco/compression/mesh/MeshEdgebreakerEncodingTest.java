package com.mndk.bteterrarenderer.draco.compression.mesh;

import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.draco.attributes.FaceIndex;
import com.mndk.bteterrarenderer.draco.attributes.GeometryAttribute;
import com.mndk.bteterrarenderer.draco.compression.DracoDecoder;
import com.mndk.bteterrarenderer.draco.compression.DracoEncoder;
import com.mndk.bteterrarenderer.draco.compression.config.DecoderOptions;
import com.mndk.bteterrarenderer.draco.compression.config.EncoderOptions;
import com.mndk.bteterrarenderer.draco.compression.config.EncoderOptionsBase;
import com.mndk.bteterrarenderer.draco.compression.config.MeshEncoderMethod;
import com.mndk.bteterrarenderer.draco.core.*;
import com.mndk.bteterrarenderer.draco.io.DracoTestFileUtil;
import com.mndk.bteterrarenderer.draco.io.MeshIOUtil;
import com.mndk.bteterrarenderer.draco.mesh.*;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;

public class MeshEdgebreakerEncodingTest {

    private void testFile(File file) {
        testFile(file, -1);
    }

    private void testFile(File file, int compressionLevel) {
        Mesh mesh = MeshIOUtil.decode(file).getValueOr(Status::throwException);
        Assert.assertNotNull("Failed to load test model " + file, mesh);
        testMesh(mesh, compressionLevel);
    }

    private void testMesh(Mesh mesh, int compressionLevel) {
        EncoderBuffer buffer = new EncoderBuffer();
        MeshEdgebreakerEncoder encoder = new MeshEdgebreakerEncoder();
        EncoderOptions encoderOptions = EncoderOptions.createDefaultOptions();
        encoderOptions.setSpeed(10 - compressionLevel, 10 - compressionLevel);
        encoder.setMesh(mesh);
        StatusAssert.assertOk(encoder.encode(encoderOptions, buffer));

        DecoderBuffer decBuffer = new DecoderBuffer();
        decBuffer.init(buffer.getData(), buffer.size());
        MeshEdgebreakerDecoder decoder = new MeshEdgebreakerDecoder();

        Mesh decodedMesh = new Mesh();
        DecoderOptions decOptions = new DecoderOptions();
        StatusAssert.assertOk(decoder.decode(decOptions, decBuffer, decodedMesh));

        MeshCleanupOptions options = new MeshCleanupOptions();
        StatusAssert.assertOk(MeshCleanup.cleanup(mesh, options));

        MeshAreEquivalent eq = new MeshAreEquivalent();
        StatusAssert.assertOk(eq.equals(mesh, decodedMesh));
    }

    @Test
    public void testNmOBJ() {
        testFile(DracoTestFileUtil.toFile("draco/testdata/test_nm.obj"));
    }

    @Test
    public void threeFacesOBJ() {
        testFile(DracoTestFileUtil.toFile("draco/testdata/extra_vertex.obj"));
    }

    // TODO Add test code for .ply files
    @Test
    public void testPly() {
        testFile(DracoTestFileUtil.toFile("draco/testdata/test_pos_color.ply"));
    }

    @Test
    public void testMultiAttributes() {
        testFile(DracoTestFileUtil.toFile("draco/testdata/cube_att.obj"), 10);
    }

    @Test
    public void testEncoderReuse() {
        File file = DracoTestFileUtil.toFile("draco/testdata/test_pos_color.ply");
        Mesh mesh = MeshIOUtil.decode(file).getValueOr(Status::throwException);
        Assert.assertNotNull("Failed to load test model " + file, mesh);

        MeshEdgebreakerEncoder encoder = new MeshEdgebreakerEncoder();
        EncoderOptions encoderOptions = EncoderOptions.createDefaultOptions();
        encoder.setMesh(mesh);
        EncoderBuffer buffer0 = new EncoderBuffer();
        EncoderBuffer buffer1 = new EncoderBuffer();
        StatusAssert.assertOk(encoder.encode(encoderOptions, buffer0));
        StatusAssert.assertOk(encoder.encode(encoderOptions, buffer1));

        Assert.assertEquals(buffer0.size(), buffer1.size());
        for (int i = 0; i < buffer0.size(); ++i) {
            Assert.assertEquals(buffer0.getData().getRawByte(i), buffer1.getData().getRawByte(i));
        }
    }

    @Test
    public void testDecoderReuse() {
        File file = DracoTestFileUtil.toFile("draco/testdata/test_pos_color.ply");
        Mesh mesh = MeshIOUtil.decode(file).getValueOr(Status::throwException);
        Assert.assertNotNull("Failed to load test model " + file, mesh);

        MeshEdgebreakerEncoder encoder = new MeshEdgebreakerEncoder();
        EncoderOptions encoderOptions = EncoderOptions.createDefaultOptions();
        encoder.setMesh(mesh);
        EncoderBuffer buffer = new EncoderBuffer();
        StatusAssert.assertOk(encoder.encode(encoderOptions, buffer));

        DecoderBuffer decBuffer = new DecoderBuffer();
        decBuffer.init(buffer.getData(), buffer.size());

        MeshEdgebreakerDecoder decoder = new MeshEdgebreakerDecoder();

        // Decode the mesh two times.
        Mesh decodedMesh0 = new Mesh();
        DecoderOptions decOptions = new DecoderOptions();
        StatusAssert.assertOk(decoder.decode(decOptions, decBuffer, decodedMesh0));

        decBuffer.init(buffer.getData(), buffer.size());
        Mesh decodedMesh1 = new Mesh();
        StatusAssert.assertOk(decoder.decode(decOptions, decBuffer, decodedMesh1));

        // Make sure both of the meshes are identical.
        MeshAreEquivalent eq = new MeshAreEquivalent();
        StatusAssert.assertOk(eq.equals(decodedMesh0, decodedMesh1));
    }

    @Test
    public void testSingleConnectivityEncoding() {
        File file = DracoTestFileUtil.toFile("draco/testdata/cube_att.obj");
        Mesh mesh = MeshIOUtil.decode(file).getValueOr(Status::throwException);
        Assert.assertNotNull("Failed to load test model " + file, mesh);

        for (int i = 0; i < 2; ++i) {
            EncoderOptionsBase<GeometryAttribute.Type> options = EncoderOptionsBase.createDefaultOptions();
            options.setGlobalBool("split_mesh_on_seams", i == 0);

            EncoderBuffer buffer = new EncoderBuffer();
            DracoEncoder encoder = new DracoEncoder();
            encoder.reset(options);
            encoder.setSpeedOptions(0, 0);
            encoder.setAttributeQuantization(GeometryAttribute.Type.POSITION, 8);
            encoder.setAttributeQuantization(GeometryAttribute.Type.TEX_COORD, 8);
            encoder.setAttributeQuantization(GeometryAttribute.Type.NORMAL, 8);
            encoder.setEncodingMethod(MeshEncoderMethod.EDGEBREAKER);
            StatusAssert.assertOk(encoder.encodeMeshToBuffer(mesh, buffer));

            DecoderBuffer decBuffer = new DecoderBuffer();
            decBuffer.init(buffer.getData(), buffer.size());

            DracoDecoder decoder = new DracoDecoder();
            Mesh decMesh = decoder.decodeMeshFromBuffer(decBuffer).getValueOr(Status::throwException);
            Assert.assertNotNull(decMesh);
            Assert.assertEquals(24, decMesh.getNumPoints());
            Assert.assertEquals(3, decMesh.getNumAttributes());
            Assert.assertEquals(i == 0 ? 24 : 8, decMesh.getAttribute(0).size());
            Assert.assertEquals(24, decMesh.getAttribute(1).size());
            Assert.assertEquals(24, decMesh.getAttribute(2).size());
        }
    }

    @Test
    public void testWrongAttributeOrder() {
        TriangleSoupMeshBuilder mb = new TriangleSoupMeshBuilder();
        mb.start(1);
        int normAttId = mb.addAttribute(GeometryAttribute.Type.NORMAL, (byte) 3, DracoDataType.FLOAT32);
        int posAttId = mb.addAttribute(GeometryAttribute.Type.POSITION, (byte) 3, DracoDataType.FLOAT32);

        mb.setAttributeValuesForFace(
            posAttId, FaceIndex.of(0), Pointer.wrap(new float[] { 0.f, 0.f, 0.f }),
            Pointer.wrap(new float[] { 1.f, 0.f, 0.f }), Pointer.wrap(new float[] { 0.f, 1.f, 0.f })
        );

        mb.setAttributeValuesForFace(
            normAttId, FaceIndex.of(0), Pointer.wrap(new float[] { 0.f, 0.f, 1.f }),
            Pointer.wrap(new float[] { 0.f, 0.f, 0.f }), Pointer.wrap(new float[] { 0.f, 0.f, 1.f })
        );
        Mesh mesh = mb.finalizeMesh();
        Assert.assertNotNull(mesh);
        Assert.assertEquals(2, mesh.getNumAttributes());
        Assert.assertEquals(GeometryAttribute.Type.NORMAL, mesh.getAttribute(0).getAttributeType());
        Assert.assertEquals(GeometryAttribute.Type.POSITION, mesh.getAttribute(1).getAttributeType());

        EncoderBuffer buffer = new EncoderBuffer();
        DracoEncoder encoder = new DracoEncoder();
        encoder.setSpeedOptions(3, 3);
        encoder.setAttributeQuantization(GeometryAttribute.Type.POSITION, 8);
        encoder.setAttributeQuantization(GeometryAttribute.Type.NORMAL, 8);
        encoder.setEncodingMethod(MeshEncoderMethod.EDGEBREAKER);
        StatusAssert.assertOk(encoder.encodeMeshToBuffer(mesh, buffer));

        DecoderBuffer decBuffer = new DecoderBuffer();
        decBuffer.init(buffer.getData(), buffer.size());

        DracoDecoder decoder = new DracoDecoder();
        Mesh decMesh = decoder.decodeMeshFromBuffer(decBuffer).getValueOr(Status::throwException);
        Assert.assertNotNull(decMesh);
        Assert.assertEquals(2, decMesh.getNumAttributes());
        Assert.assertEquals(GeometryAttribute.Type.POSITION, decMesh.getAttribute(0).getAttributeType());
        Assert.assertEquals(GeometryAttribute.Type.NORMAL, decMesh.getAttribute(1).getAttributeType());
    }

    @Test
    public void testDegenerateMesh() {
        File file = DracoTestFileUtil.toFile("draco/testdata/degenerate_mesh.obj");
        Mesh mesh = MeshIOUtil.decode(file).getValueOr(Status::throwException);
        Assert.assertNotNull("Failed to load test model " + file, mesh);
        EncoderBuffer buffer = new EncoderBuffer();
        MeshEdgebreakerEncoder encoder = new MeshEdgebreakerEncoder();
        EncoderOptions encoderOptions = EncoderOptions.createDefaultOptions();
        encoder.setMesh(mesh);
        StatusAssert.assertError(encoder.encode(encoderOptions, buffer));
    }

}
