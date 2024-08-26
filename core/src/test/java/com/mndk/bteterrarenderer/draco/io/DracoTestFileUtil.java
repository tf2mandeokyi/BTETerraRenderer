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

import com.mndk.bteterrarenderer.datatype.pointer.PointerHelper;
import com.mndk.bteterrarenderer.datatype.pointer.RawPointer;
import com.mndk.bteterrarenderer.draco.core.DecoderBuffer;
import com.mndk.bteterrarenderer.draco.core.EncoderBuffer;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.draco.mesh.Mesh;
import com.mndk.bteterrarenderer.printer.ByteTablePrinter;
import com.mndk.bteterrarenderer.printer.TableColumnPrinter;
import com.mndk.bteterrarenderer.printer.TablePrinter;
import org.junit.Assert;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;

public class DracoTestFileUtil {

    public static File toFile(String fileName) {
        URL url = DracoTestFileUtil.class.getClassLoader().getResource(fileName);
        if(url == null) Assert.fail("File not found: " + fileName);
        try {
            return new File(url.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static void compareGoldenFile(File goldenFile, EncoderBuffer actualBuffer) {
        DecoderBuffer goldenBuffer = new DecoderBuffer();
        try(InputStream inputStream = Files.newInputStream(goldenFile.toPath())) {
            goldenBuffer.init(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        RawPointer pointer = actualBuffer.getData();
        RawPointer goldenPointer = goldenBuffer.getData();
        long actualSize = actualBuffer.size();
        long goldenSize = goldenBuffer.getRemainingSize();
        long byteDifference = PointerHelper.searchRawDifference(pointer, goldenPointer, Math.min(goldenSize, actualSize));
        if(byteDifference != -1) {
            TablePrinter tablePrinter = new TablePrinter(" | ");
            TableColumnPrinter actual = tablePrinter.newColumn();
            TableColumnPrinter golden = tablePrinter.newColumn();
            actual.print("### Output data (size = " + actualSize + ") ###\n");
            golden.print("### Golden file data (size = " + goldenSize + ") ###\n");
            ByteTablePrinter.print(actual, pointer, actualSize);
            ByteTablePrinter.print(golden, goldenPointer, goldenSize);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            tablePrinter.print(new PrintStream(baos));
            String table = baos.toString();
            String content = "Output does not match the content of the golden file ("
                    + goldenFile.getName() + ")" + " at byte position 0x" + Long.toHexString(byteDifference) + "."
                    + "\n\n" + table;
            System.err.println(content);
            Assert.fail();
        }
    }
    
    public static void testDecoding(String fileName) {
        testDecoding(toFile(fileName));
    }

    public static void testDecoding(File file) {
        Mesh mesh = MeshIOUtil.decode(file).getValueOr(Status::throwException);
        Assert.assertTrue(mesh.getNumFaces() > 0);

        Mesh pointCloud = MeshIOUtil.decode(file).getValueOr(Status::throwException);
        Assert.assertTrue(pointCloud.getNumPoints() > 0);
    }
}
