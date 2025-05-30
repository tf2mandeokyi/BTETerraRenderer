/*
 * Copyright (C) 2024 The Draco Authors (for providing the original C++ code)
 * Copyright (C) 2024 m4ndeokyi (for translating the code into Java)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mndk.bteterrarenderer.draco.mesh;

import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.draco.io.DracoTestFileUtil;
import com.mndk.bteterrarenderer.draco.core.StatusAssert;
import com.mndk.bteterrarenderer.draco.io.MeshIOUtil;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;

public class MeshAreEquivalentTest {

    @Test
    public void testOnIdenticalMesh() {
        File file = DracoTestFileUtil.toFile("draco/testdata/test_nm.obj");
        Mesh mesh = MeshIOUtil.decode(file).getValueOr(Status::throwException);
        Assert.assertNotNull("Failed to load test model: " + file, mesh);

        MeshAreEquivalent equiv = new MeshAreEquivalent();
        StatusAssert.assertOk(equiv.equals(mesh, mesh));
    }

    @Test
    public void testPermutedOneFace() {
        File file0 = DracoTestFileUtil.toFile("draco/testdata/one_face_123.obj");
        File file1 = DracoTestFileUtil.toFile("draco/testdata/one_face_312.obj");
        File file2 = DracoTestFileUtil.toFile("draco/testdata/one_face_321.obj");
        Mesh mesh0 = MeshIOUtil.decode(file0).getValueOr(Status::throwException);
        Mesh mesh1 = MeshIOUtil.decode(file1).getValueOr(Status::throwException);
        Mesh mesh2 = MeshIOUtil.decode(file2).getValueOr(Status::throwException);
        Assert.assertNotNull("Failed to load test model: " + file0, mesh0);
        Assert.assertNotNull("Failed to load test model: " + file1, mesh1);
        Assert.assertNotNull("Failed to load test model: " + file2, mesh2);

        MeshAreEquivalent equiv = new MeshAreEquivalent();
        StatusAssert.assertOk(equiv.equals(mesh0, mesh0));
        StatusAssert.assertOk(equiv.equals(mesh0, mesh1)); // Face rotated
        StatusAssert.assertError(equiv.equals(mesh0, mesh2)); // Face inverted
    }

    @Test
    public void testPermutedTwoFaces() {
        File file0 = DracoTestFileUtil.toFile("draco/testdata/two_faces_123.obj");
        File file1 = DracoTestFileUtil.toFile("draco/testdata/two_faces_312.obj");
        Mesh mesh0 = MeshIOUtil.decode(file0).getValueOr(Status::throwException);
        Mesh mesh1 = MeshIOUtil.decode(file1).getValueOr(Status::throwException);
        Assert.assertNotNull("Failed to load test model: " + file0, mesh0);
        Assert.assertNotNull("Failed to load test model: " + file1, mesh1);

        MeshAreEquivalent equiv = new MeshAreEquivalent();
        StatusAssert.assertOk(equiv.equals(mesh0, mesh0));
        StatusAssert.assertOk(equiv.equals(mesh1, mesh1));
        StatusAssert.assertOk(equiv.equals(mesh0, mesh1));
    }

    @Test
    public void testPermutedThreeFaces() {
        File file0 = DracoTestFileUtil.toFile("draco/testdata/three_faces_123.obj");
        File file1 = DracoTestFileUtil.toFile("draco/testdata/three_faces_312.obj");
        Mesh mesh0 = MeshIOUtil.decode(file0).getValueOr(Status::throwException);
        Mesh mesh1 = MeshIOUtil.decode(file1).getValueOr(Status::throwException);
        Assert.assertNotNull("Failed to load test model: " + file0, mesh0);
        Assert.assertNotNull("Failed to load test model: " + file1, mesh1);

        MeshAreEquivalent equiv = new MeshAreEquivalent();
        StatusAssert.assertOk(equiv.equals(mesh0, mesh0));
        StatusAssert.assertOk(equiv.equals(mesh1, mesh1));
        StatusAssert.assertOk(equiv.equals(mesh0, mesh1));
    }

    @Test
    public void testOnBigMesh() {
        File file = DracoTestFileUtil.toFile("draco/testdata/test_nm.obj");
        Mesh mesh0 = MeshIOUtil.decode(file).getValueOr(Status::throwException);
        Assert.assertNotNull("Failed to load test model: " + file, mesh0);

        Mesh mesh1 = MeshIOUtil.decode(file).getValueOr(Status::throwException);
        Assert.assertNotNull("Failed to load test model: " + file, mesh1);

        MeshAreEquivalent equiv = new MeshAreEquivalent();
        StatusAssert.assertOk(equiv.equals(mesh0, mesh0));
        StatusAssert.assertOk(equiv.equals(mesh1, mesh1));
        StatusAssert.assertOk(equiv.equals(mesh0, mesh1));
    }

}
