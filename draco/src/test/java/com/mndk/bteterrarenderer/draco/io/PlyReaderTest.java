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

package com.mndk.bteterrarenderer.draco.io;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.UByte;
import com.mndk.bteterrarenderer.datatype.number.UInt;
import com.mndk.bteterrarenderer.draco.core.DecoderBuffer;
import com.mndk.bteterrarenderer.draco.core.StatusAssert;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;

public class PlyReaderTest {

    @Test
    public void testReader() {
        File file = DracoTestFileUtil.toFile("draco/testdata/test_pos_color.ply");
        DecoderBuffer buf = new DecoderBuffer();
        try (InputStream stream = Files.newInputStream(file.toPath())) {
            buf.init(stream);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        PlyReader reader = new PlyReader();
        StatusAssert.assertOk(reader.read(buf));
        Assert.assertEquals(2, reader.getNumElements());
        Assert.assertEquals(7, reader.getElement(0).getNumProperties());
        Assert.assertEquals(1, reader.getElement(1).getNumProperties());
        Assert.assertTrue(reader.getElement(1).getProperty(0).isList());

        Assert.assertNotNull(reader.getElement(0).getPropertyByName("red"));
        PlyProperty prop = reader.getElement(0).getPropertyByName("red");
        PlyPropertyReader<UByte> readerUInt8 = new PlyPropertyReader<>(DataType.uint8(), prop);
        PlyPropertyReader<UInt> readerUInt32 = new PlyPropertyReader<>(DataType.uint32(), prop);
        PlyPropertyReader<Float> readerFloat = new PlyPropertyReader<>(DataType.float32(), prop);
        for (int i = 0; i < reader.getElement(0).getNumEntries(); i++) {
            Assert.assertEquals(readerUInt8.readValue(i).intValue(), readerUInt32.readValue(i).intValue());
            Assert.assertEquals(readerUInt8.readValue(i).floatValue(), readerFloat.readValue(i), 1e-4f);
        }
    }

    @Test
    public void testReaderAscii() {
        File file = DracoTestFileUtil.toFile("draco/testdata/test_pos_color.ply");
        DecoderBuffer buf = new DecoderBuffer();
        PlyReader reader = new PlyReader();
        try (InputStream stream = Files.newInputStream(file.toPath())) {
            buf.init(stream);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        StatusAssert.assertOk(reader.read(buf));

        File fileAscii = DracoTestFileUtil.toFile("draco/testdata/test_pos_color_ascii.ply");
        DecoderBuffer bufAscii = new DecoderBuffer();
        PlyReader readerAscii = new PlyReader();
        try (InputStream stream = Files.newInputStream(fileAscii.toPath())) {
            bufAscii.init(stream);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        StatusAssert.assertOk(readerAscii.read(bufAscii));
        Assert.assertEquals(reader.getNumElements(), readerAscii.getNumElements());
        Assert.assertEquals(reader.getElement(0).getNumProperties(), readerAscii.getElement(0).getNumProperties());

        Assert.assertNotNull(reader.getElement(0).getPropertyByName("x"));
        PlyProperty prop = reader.getElement(0).getPropertyByName("x");
        PlyProperty propAscii = readerAscii.getElement(0).getPropertyByName("x");
        PlyPropertyReader<Float> readerFloat = new PlyPropertyReader<>(DataType.float32(), prop);
        PlyPropertyReader<Float> readerFloatAscii = new PlyPropertyReader<>(DataType.float32(), propAscii);
        for (int i = 0; i < reader.getElement(0).getNumEntries(); i++) {
            Assert.assertEquals(readerFloat.readValue(i), readerFloatAscii.readValue(i), 1e-4f);
        }
    }

    @Test
    public void testReaderExtraWhitespace() {
        File file = DracoTestFileUtil.toFile("draco/testdata/test_extra_whitespace.ply");
        DecoderBuffer buf = new DecoderBuffer();
        PlyReader reader = new PlyReader();
        try (InputStream stream = Files.newInputStream(file.toPath())) {
            buf.init(stream);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        StatusAssert.assertOk(reader.read(buf));

        Assert.assertEquals(2, reader.getNumElements());
        Assert.assertEquals(7, reader.getElement(0).getNumProperties());
        Assert.assertEquals(1, reader.getElement(1).getNumProperties());
        Assert.assertTrue(reader.getElement(1).getProperty(0).isList());

        Assert.assertNotNull(reader.getElement(0).getPropertyByName("red"));
        PlyProperty prop = reader.getElement(0).getPropertyByName("red");
        PlyPropertyReader<UByte> readerUInt8 = new PlyPropertyReader<>(DataType.uint8(), prop);
        PlyPropertyReader<UInt> readerUInt32 = new PlyPropertyReader<>(DataType.uint32(), prop);
        PlyPropertyReader<Float> readerFloat = new PlyPropertyReader<>(DataType.float32(), prop);
        for (int i = 0; i < reader.getElement(0).getNumEntries(); i++) {
            Assert.assertEquals(readerUInt8.readValue(i).intValue(), readerUInt32.readValue(i).intValue());
            Assert.assertEquals(readerUInt8.readValue(i).floatValue(), readerFloat.readValue(i), 1e-4f);
        }
    }

    @Test
    public void testReaderMoreDataTypes() {
        File file = DracoTestFileUtil.toFile("draco/testdata/test_more_datatypes.ply");
        DecoderBuffer buf = new DecoderBuffer();
        PlyReader reader = new PlyReader();
        try (InputStream stream = Files.newInputStream(file.toPath())) {
            buf.init(stream);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        StatusAssert.assertOk(reader.read(buf));

        Assert.assertEquals(2, reader.getNumElements());
        Assert.assertEquals(7, reader.getElement(0).getNumProperties());
        Assert.assertEquals(1, reader.getElement(1).getNumProperties());
        Assert.assertTrue(reader.getElement(1).getProperty(0).isList());

        Assert.assertNotNull(reader.getElement(0).getPropertyByName("red"));
        PlyProperty prop = reader.getElement(0).getPropertyByName("red");
        PlyPropertyReader<UByte> readerUInt8 = new PlyPropertyReader<>(DataType.uint8(), prop);
        PlyPropertyReader<UInt> readerUInt32 = new PlyPropertyReader<>(DataType.uint32(), prop);
        PlyPropertyReader<Float> readerFloat = new PlyPropertyReader<>(DataType.float32(), prop);
        for (int i = 0; i < reader.getElement(0).getNumEntries(); i++) {
            Assert.assertEquals(readerUInt8.readValue(i).intValue(), readerUInt32.readValue(i).intValue());
            Assert.assertEquals(readerUInt8.readValue(i).floatValue(), readerFloat.readValue(i), 1e-4f);
        }
    }

}
