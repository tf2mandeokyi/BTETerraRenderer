//// Copyright 2016 The Draco Authors.
////
//// Licensed under the Apache License, Version 2.0 (the "License");
//// you may not use this file except in compliance with the License.
//// You may obtain a copy of the License at
////
////      http://www.apache.org/licenses/LICENSE-2.0
////
//// Unless required by applicable law or agreed to in writing, software
//// distributed under the License is distributed on an "AS IS" BASIS,
//// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//// See the License for the specific language governing permissions and
//// limitations under the License.
////
//#include "draco/mesh/mesh_cleanup.h"
//
//#include "draco/core/draco_test_base.h"
//#include "draco/core/draco_test_utils.h"
//#include "draco/core/vector_d.h"
//#include "draco/mesh/triangle_soup_mesh_builder.h"
//
//namespace draco {
//
//class MeshCleanupTest : public ::testing::Test {};
//
//TEST_F(MeshCleanupTest, TestDegneratedFaces) {
//  // This test verifies that the mesh cleanup tools removes degenerated faces.
//  TriangleSoupMeshBuilder mb;
//  mb.Start(2);
//  const int pos_att_id =
//      mb.AddAttribute(GeometryAttribute::POSITION, 3, DT_FLOAT32);
//  // clang-format off
//  mb.SetAttributeValuesForFace(pos_att_id, FaceIndex(0),
//                               Vector3f(0.f, 0.f, 0.f).data(),
//                               Vector3f(1.f, 0.f, 0.f).data(),
//                               Vector3f(0.f, 1.f, 0.f).data());
//  mb.SetAttributeValuesForFace(pos_att_id, FaceIndex(1),
//                               Vector3f(0.f, 1.f, 0.f).data(),
//                               Vector3f(1.f, 0.f, 0.f).data(),
//                               Vector3f(1.f, 0.f, 0.f).data());
//  // clang-format on
//
//  std::unique_ptr<Mesh> mesh = mb.Finalize();
//  ASSERT_NE(mesh, nullptr) << "Failed to build the test mesh.";
//  ASSERT_EQ(mesh->num_faces(), 2) << "Wrong number of faces in the input mesh.";
//  MeshCleanupOptions cleanup_options;
//  DRACO_ASSERT_OK(MeshCleanup::Cleanup(mesh.get(), cleanup_options));
//  ASSERT_EQ(mesh->num_faces(), 1) << "Failed to remove degenerated faces.";
//}
//
//TEST_F(MeshCleanupTest, TestDegneratedFacesAndIsolatedVertices) {
//  // This test verifies that the mesh cleanup tools removes degenerated faces
//  // and isolated vertices.
//  TriangleSoupMeshBuilder mb;
//  mb.Start(2);
//  const int pos_att_id =
//      mb.AddAttribute(GeometryAttribute::POSITION, 3, DT_FLOAT32);
//
//  // Dummy integer attribute for which we do not expect the number of entries
//  // to change after the degnerate face and isolated vertex are removed.
//  const int int_att_id =
//      mb.AddAttribute(GeometryAttribute::GENERIC, 2, DT_INT32);
//
//  // clang-format off
//  mb.SetAttributeValuesForFace(pos_att_id, FaceIndex(0),
//                               Vector3f(0.f, 0.f, 0.f).data(),
//                               Vector3f(1.f, 0.f, 0.f).data(),
//                               Vector3f(0.f, 1.f, 0.f).data());
//  mb.SetAttributeValuesForFace(int_att_id, FaceIndex(0),
//                               VectorD<int32_t, 2>(0, 0).data(),
//                               VectorD<int32_t, 2>(0, 1).data(),
//                               VectorD<int32_t, 2>(0, 2).data());
//
//  mb.SetAttributeValuesForFace(pos_att_id, FaceIndex(1),
//                               Vector3f(10.f, 1.f, 0.f).data(),
//                               Vector3f(1.f, 0.f, 0.f).data(),
//                               Vector3f(10.f, 1.f, 0.f).data());
//  mb.SetAttributeValuesForFace(int_att_id, FaceIndex(1),
//                               VectorD<int32_t, 2>(0, 0).data(),
//                               VectorD<int32_t, 2>(0, 1).data(),
//                               VectorD<int32_t, 2>(0, 2).data());
//  // clang-format on
//
//  std::unique_ptr<Mesh> mesh = mb.Finalize();
//  ASSERT_NE(mesh, nullptr) << "Failed to build the test mesh.";
//  ASSERT_EQ(mesh->num_faces(), 2) << "Wrong number of faces in the input mesh.";
//  ASSERT_EQ(mesh->num_points(), 5)
//      << "Wrong number of point ids in the input mesh.";
//  ASSERT_EQ(mesh->attribute(int_att_id)->size(), 3);
//  const MeshCleanupOptions cleanup_options;
//  DRACO_ASSERT_OK(MeshCleanup::Cleanup(mesh.get(), cleanup_options));
//  ASSERT_EQ(mesh->num_faces(), 1) << "Failed to remove degenerated faces.";
//  ASSERT_EQ(mesh->num_points(), 3)
//      << "Failed to remove isolated attribute indices.";
//  ASSERT_EQ(mesh->attribute(int_att_id)->size(), 3);
//}
//
//TEST_F(MeshCleanupTest, TestAttributes) {
//  TriangleSoupMeshBuilder mb;
//  mb.Start(2);
//  const int pos_att_id =
//      mb.AddAttribute(GeometryAttribute::POSITION, 3, DT_FLOAT32);
//  const int generic_att_id =
//      mb.AddAttribute(GeometryAttribute::GENERIC, 2, DT_FLOAT32);
//  // clang-format off
//  mb.SetAttributeValuesForFace(pos_att_id, FaceIndex(0),
//                               Vector3f(0.f, 0.f, 0.f).data(),
//                               Vector3f(1.f, 0.f, 0.f).data(),
//                               Vector3f(0.f, 1.f, 0.f).data());
//  mb.SetAttributeValuesForFace(generic_att_id, FaceIndex(0),
//                               Vector2f(0.f, 0.f).data(),
//                               Vector2f(0.f, 0.f).data(),
//                               Vector2f(0.f, 0.f).data());
//
//  mb.SetAttributeValuesForFace(pos_att_id, FaceIndex(1),
//                               Vector3f(10.f, 1.f, 0.f).data(),
//                               Vector3f(1.f, 0.f, 0.f).data(),
//                               Vector3f(10.f, 1.f, 0.f).data());
//  mb.SetAttributeValuesForFace(generic_att_id, FaceIndex(1),
//                               Vector2f(1.f, 0.f).data(),
//                               Vector2f(1.f, 0.f).data(),
//                               Vector2f(1.f, 0.f).data());
//  // clang-format on
//
//  std::unique_ptr<Mesh> mesh = mb.Finalize();
//  ASSERT_NE(mesh, nullptr) << "Failed to build the test mesh.";
//  ASSERT_EQ(mesh->num_faces(), 2) << "Wrong number of faces in the input mesh.";
//  ASSERT_EQ(mesh->num_points(), 5)
//      << "Wrong number of point ids in the input mesh.";
//  ASSERT_EQ(mesh->attribute(1)->size(), 2u)
//      << "Wrong number of generic attribute entries.";
//  const MeshCleanupOptions cleanup_options;
//  DRACO_ASSERT_OK(MeshCleanup::Cleanup(mesh.get(), cleanup_options));
//  ASSERT_EQ(mesh->num_faces(), 1) << "Failed to remove degenerated faces.";
//  ASSERT_EQ(mesh->num_points(), 3)
//      << "Failed to remove isolated attribute indices.";
//  ASSERT_EQ(mesh->attribute(0)->size(), 3u)
//      << "Wrong number of unique positions after cleanup.";
//  ASSERT_EQ(mesh->attribute(1)->size(), 1u)
//      << "Wrong number of generic attribute entries after cleanup.";
//}
//
//TEST_F(MeshCleanupTest, TestDuplicateFaces) {
//  // This test verifies that the mesh cleanup tool removes duplicate faces.
//  TriangleSoupMeshBuilder mb;
//  mb.Start(5);
//  const int pos_att_id =
//      mb.AddAttribute(GeometryAttribute::POSITION, 3, DT_FLOAT32);
//  const int norm_att_id =
//      mb.AddAttribute(GeometryAttribute::NORMAL, 3, DT_FLOAT32);
//
//  // Five faces where only two are unique in spatial domain and three are unique
//  // when we take into account the normal attribute.
//
//  // clang-format off
//  mb.SetAttributeValuesForFace(pos_att_id, FaceIndex(0),
//                               Vector3f(0.f, 0.f, 0.f).data(),
//                               Vector3f(1.f, 0.f, 0.f).data(),
//                               Vector3f(0.f, 1.f, 0.f).data());
//  mb.SetAttributeValuesForFace(norm_att_id, FaceIndex(0),
//                               Vector3f(0.f, 0.f, 1.f).data(),
//                               Vector3f(0.f, 0.f, 1.f).data(),
//                               Vector3f(0.f, 0.f, 1.f).data());
//
//  mb.SetAttributeValuesForFace(pos_att_id, FaceIndex(1),
//                               Vector3f(0.f, 0.f, 0.f).data(),
//                               Vector3f(1.f, 0.f, 0.f).data(),
//                               Vector3f(0.f, 1.f, 0.f).data());
//  mb.SetAttributeValuesForFace(norm_att_id, FaceIndex(1),
//                               Vector3f(0.f, 1.f, 0.f).data(),
//                               Vector3f(0.f, 1.f, 0.f).data(),
//                               Vector3f(0.f, 1.f, 0.f).data());
//
//  mb.SetAttributeValuesForFace(pos_att_id, FaceIndex(2),
//                               Vector3f(0.f, 0.f, 0.f).data(),
//                               Vector3f(1.f, 0.f, 0.f).data(),
//                               Vector3f(0.f, 1.f, 1.f).data());
//  mb.SetAttributeValuesForFace(norm_att_id, FaceIndex(2),
//                               Vector3f(0.f, 0.f, 1.f).data(),
//                               Vector3f(0.f, 0.f, 1.f).data(),
//                               Vector3f(0.f, 0.f, 1.f).data());
//
//  mb.SetAttributeValuesForFace(pos_att_id, FaceIndex(3),
//                               Vector3f(1.f, 0.f, 0.f).data(),
//                               Vector3f(0.f, 1.f, 0.f).data(),
//                              Vector3f(0.f, 0.f, 0.f).data());
//  mb.SetAttributeValuesForFace(norm_att_id, FaceIndex(3),
//                               Vector3f(0.f, 0.f, 1.f).data(),
//                               Vector3f(0.f, 0.f, 1.f).data(),
//                               Vector3f(0.f, 0.f, 1.f).data());
//
//  mb.SetAttributeValuesForFace(pos_att_id, FaceIndex(4),
//                               Vector3f(0.f, 0.f, 0.f).data(),
//                               Vector3f(1.f, 0.f, 0.f).data(),
//                               Vector3f(0.f, 1.f, 1.f).data());
//  mb.SetAttributeValuesForFace(norm_att_id, FaceIndex(4),
//                               Vector3f(0.f, 0.f, 1.f).data(),
//                               Vector3f(0.f, 0.f, 1.f).data(),
//                               Vector3f(0.f, 0.f, 1.f).data());
//  // clang-format on
//
//  std::unique_ptr<Mesh> mesh = mb.Finalize();
//  ASSERT_NE(mesh, nullptr);
//  ASSERT_EQ(mesh->num_faces(), 5);
//  const MeshCleanupOptions cleanup_options;
//  DRACO_ASSERT_OK(MeshCleanup::Cleanup(mesh.get(), cleanup_options));
//  ASSERT_EQ(mesh->num_faces(), 3);
//}
//
//}  // namespace draco

package com.mndk.bteterrarenderer.draco.mesh;

import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.draco.attributes.FaceIndex;
import com.mndk.bteterrarenderer.draco.attributes.GeometryAttribute;
import com.mndk.bteterrarenderer.draco.core.DracoDataType;
import com.mndk.bteterrarenderer.draco.core.StatusAssert;
import org.junit.Assert;
import org.junit.Test;

public class MeshCleanupTest {

    @Test
    public void testDegeneratedFaces() {
        // This test verifies that the mesh cleanup tools removes degenerated faces.
        TriangleSoupMeshBuilder mb = new TriangleSoupMeshBuilder();
        mb.start(2);
        int posAttId = mb.addAttribute(GeometryAttribute.Type.POSITION, (byte) 3, DracoDataType.FLOAT32);
        mb.setAttributeValuesForFace(posAttId, FaceIndex.of(0),
                Pointer.wrap(new float[] { 0f, 0f, 0f }),
                Pointer.wrap(new float[] { 1f, 0f, 0f }),
                Pointer.wrap(new float[] { 0f, 1f, 0f }));
        mb.setAttributeValuesForFace(posAttId, FaceIndex.of(1),
                Pointer.wrap(new float[] { 0f, 1f, 0f }),
                Pointer.wrap(new float[] { 1f, 0f, 0f }),
                Pointer.wrap(new float[] { 1f, 0f, 0f }));

        Mesh mesh = mb.finalizeMesh();
        Assert.assertNotNull("Failed to build the test mesh.", mesh);
        Assert.assertEquals("Wrong number of faces in the input mesh.", 2, mesh.getNumFaces());
        MeshCleanupOptions cleanupOptions = new MeshCleanupOptions();
        StatusAssert.assertOk(MeshCleanup.cleanup(mesh, cleanupOptions));
        Assert.assertEquals("Failed to remove degenerated faces.", 1, mesh.getNumFaces());
    }

    @Test
    public void testDegeneratedFacesAndIsolatedVertices() {
        // This test verifies that the mesh cleanup tools removes degenerated faces
        // and isolated vertices.
        TriangleSoupMeshBuilder mb = new TriangleSoupMeshBuilder();
        mb.start(2);
        int posAttId = mb.addAttribute(GeometryAttribute.Type.POSITION, (byte) 3, DracoDataType.FLOAT32);
        int intAttId = mb.addAttribute(GeometryAttribute.Type.GENERIC, (byte) 2, DracoDataType.INT32);
        mb.setAttributeValuesForFace(posAttId, FaceIndex.of(0),
                Pointer.wrap(new float[] { 0f, 0f, 0f }),
                Pointer.wrap(new float[] { 1f, 0f, 0f }),
                Pointer.wrap(new float[] { 0f, 1f, 0f }));
        mb.setAttributeValuesForFace(intAttId, FaceIndex.of(0),
                Pointer.wrap(new int[] { 0, 0 }),
                Pointer.wrap(new int[] { 0, 1 }),
                Pointer.wrap(new int[] { 0, 2 }));

        mb.setAttributeValuesForFace(posAttId, FaceIndex.of(1),
                Pointer.wrap(new float[] { 10f, 1f, 0f }),
                Pointer.wrap(new float[] { 1f, 0f, 0f }),
                Pointer.wrap(new float[] { 10f, 1f, 0f }));
        mb.setAttributeValuesForFace(intAttId, FaceIndex.of(1),
                Pointer.wrap(new int[] { 0, 0 }),
                Pointer.wrap(new int[] { 0, 1 }),
                Pointer.wrap(new int[] { 0, 2 }));

        Mesh mesh = mb.finalizeMesh();
        Assert.assertNotNull("Failed to build the test mesh.", mesh);
        Assert.assertEquals("Wrong number of faces in the input mesh.", 2, mesh.getNumFaces());
        Assert.assertEquals("Wrong number of point ids in the input mesh.", 5, mesh.getNumPoints());
        Assert.assertEquals(3, mesh.getAttribute(intAttId).size());
        MeshCleanupOptions cleanupOptions = new MeshCleanupOptions();
        StatusAssert.assertOk(MeshCleanup.cleanup(mesh, cleanupOptions));
        Assert.assertEquals("Failed to remove degenerated faces.", 1, mesh.getNumFaces());
        Assert.assertEquals("Failed to remove isolated attribute indices.", 3, mesh.getNumPoints());
        Assert.assertEquals(3, mesh.getAttribute(intAttId).size());
    }

    @Test
    public void testAttributes() {
        TriangleSoupMeshBuilder mb = new TriangleSoupMeshBuilder();
        mb.start(2);
        int posAttId = mb.addAttribute(GeometryAttribute.Type.POSITION, (byte) 3, DracoDataType.FLOAT32);
        int genericAttId = mb.addAttribute(GeometryAttribute.Type.GENERIC, (byte) 2, DracoDataType.FLOAT32);
        mb.setAttributeValuesForFace(posAttId, FaceIndex.of(0),
                Pointer.wrap(new float[] { 0f, 0f, 0f }),
                Pointer.wrap(new float[] { 1f, 0f, 0f }),
                Pointer.wrap(new float[] { 0f, 1f, 0f }));
        mb.setAttributeValuesForFace(genericAttId, FaceIndex.of(0),
                Pointer.wrap(new float[] { 0f, 0f }),
                Pointer.wrap(new float[] { 0f, 0f }),
                Pointer.wrap(new float[] { 0f, 0f }));

        mb.setAttributeValuesForFace(posAttId, FaceIndex.of(1),
                Pointer.wrap(new float[] { 10f, 1f, 0f }),
                Pointer.wrap(new float[] { 1f, 0f, 0f }),
                Pointer.wrap(new float[] { 10f, 1f, 0f }));
        mb.setAttributeValuesForFace(genericAttId, FaceIndex.of(1),
                Pointer.wrap(new float[] { 1f, 0f }),
                Pointer.wrap(new float[] { 1f, 0f }),
                Pointer.wrap(new float[] { 1f, 0f }));

        Mesh mesh = mb.finalizeMesh();
        Assert.assertNotNull("Failed to build the test mesh.", mesh);
        Assert.assertEquals("Wrong number of faces in the input mesh.", 2, mesh.getNumFaces());
        Assert.assertEquals("Wrong number of point ids in the input mesh.", 5, mesh.getNumPoints());
        Assert.assertEquals(2, mesh.getAttribute(genericAttId).size());
        MeshCleanupOptions cleanupOptions = new MeshCleanupOptions();
        StatusAssert.assertOk(MeshCleanup.cleanup(mesh, cleanupOptions));
        Assert.assertEquals("Failed to remove degenerated faces.", 1, mesh.getNumFaces());
        Assert.assertEquals("Failed to remove isolated attribute indices.", 3, mesh.getNumPoints());
        Assert.assertEquals(3, mesh.getAttribute(0).size());
        Assert.assertEquals(1, mesh.getAttribute(1).size());
    }

    @Test
    public void testDuplicateFaces() {
        TriangleSoupMeshBuilder mb = new TriangleSoupMeshBuilder();
        mb.start(5);
        int posAttId = mb.addAttribute(GeometryAttribute.Type.POSITION, (byte) 3, DracoDataType.FLOAT32);
        int normAttId = mb.addAttribute(GeometryAttribute.Type.NORMAL, (byte) 3, DracoDataType.FLOAT32);

        // Five faces where only two are unique in spatial domain and three are unique
        // when we take into account the normal attribute.

        // clang-format off
        mb.setAttributeValuesForFace(posAttId, FaceIndex.of(0),
                Pointer.wrap(new float[] { 0f, 0f, 0f }),
                Pointer.wrap(new float[] { 1f, 0f, 0f }),
                Pointer.wrap(new float[] { 0f, 1f, 0f }));
        mb.setAttributeValuesForFace(normAttId, FaceIndex.of(0),
                Pointer.wrap(new float[] { 0f, 0f, 1f }),
                Pointer.wrap(new float[] { 0f, 0f, 1f }),
                Pointer.wrap(new float[] { 0f, 0f, 1f }));

        mb.setAttributeValuesForFace(posAttId, FaceIndex.of(1),
                Pointer.wrap(new float[] { 0f, 0f, 0f }),
                Pointer.wrap(new float[] { 1f, 0f, 0f }),
                Pointer.wrap(new float[] { 0f, 1f, 0f }));
        mb.setAttributeValuesForFace(normAttId, FaceIndex.of(1),
                Pointer.wrap(new float[] { 0f, 1f, 0f }),
                Pointer.wrap(new float[] { 0f, 1f, 0f }),
                Pointer.wrap(new float[] { 0f, 1f, 0f }));

        mb.setAttributeValuesForFace(posAttId, FaceIndex.of(2),
                Pointer.wrap(new float[] { 0f, 0f, 0f }),
                Pointer.wrap(new float[] { 1f, 0f, 0f }),
                Pointer.wrap(new float[] { 0f, 1f, 1f }));
        mb.setAttributeValuesForFace(normAttId, FaceIndex.of(2),
                Pointer.wrap(new float[] { 0f, 0f, 1f }),
                Pointer.wrap(new float[] { 0f, 0f, 1f }),
                Pointer.wrap(new float[] { 0f, 0f, 1f }));

        mb.setAttributeValuesForFace(posAttId, FaceIndex.of(3),
                Pointer.wrap(new float[] { 1f, 0f, 0f }),
                Pointer.wrap(new float[] { 0f, 1f, 0f }),
                Pointer.wrap(new float[] { 0f, 0f, 0f }));
        mb.setAttributeValuesForFace(normAttId, FaceIndex.of(3),
                Pointer.wrap(new float[] { 0f, 0f, 1f }),
                Pointer.wrap(new float[] { 0f, 0f, 1f }),
                Pointer.wrap(new float[] { 0f, 0f, 1f }));

        mb.setAttributeValuesForFace(posAttId, FaceIndex.of(4),
                Pointer.wrap(new float[] { 0f, 0f, 0f }),
                Pointer.wrap(new float[] { 1f, 0f, 0f }),
                Pointer.wrap(new float[] { 0f, 1f, 1f }));
        mb.setAttributeValuesForFace(normAttId, FaceIndex.of(4),
                Pointer.wrap(new float[] { 0f, 0f, 1f }),
                Pointer.wrap(new float[] { 0f, 0f, 1f }),
                Pointer.wrap(new float[] { 0f, 0f, 1f }));

        Mesh mesh = mb.finalizeMesh();
        Assert.assertNotNull(mesh);
        Assert.assertEquals(5, mesh.getNumFaces());
        MeshCleanupOptions cleanupOptions = new MeshCleanupOptions();
        StatusAssert.assertOk(MeshCleanup.cleanup(mesh, cleanupOptions));
        Assert.assertEquals(3, mesh.getNumFaces());
    }

}
