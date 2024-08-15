package com.mndk.bteterrarenderer.draco.compression;

import com.mndk.bteterrarenderer.datatype.pointer.PointerHelper;
import com.mndk.bteterrarenderer.datatype.pointer.RawPointer;
import com.mndk.bteterrarenderer.draco.core.*;
import com.mndk.bteterrarenderer.draco.io.ObjDecoder;
import com.mndk.bteterrarenderer.draco.mesh.Mesh;
import com.mndk.bteterrarenderer.printer.ByteTablePrinter;
import com.mndk.bteterrarenderer.printer.TableColumnPrinter;
import com.mndk.bteterrarenderer.printer.TablePrinter;
import org.junit.Assert;

import java.io.*;
import java.net.URL;

public class DracoTestFileUtil {

    public static InputStream fileToInputStream(String fileName) {
        InputStream stream = DracoTestFileUtil.class.getClassLoader().getResourceAsStream(fileName);
        if(stream == null) {
            throw new RuntimeException("File not found: " + fileName);
        }
        return stream;
    }

    public static Mesh decodeDraco(String fileName) {
        DecoderBuffer decoderBuffer = new DecoderBuffer();
        try(InputStream stream = fileToInputStream(fileName)) {
            decoderBuffer.init(stream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        DracoDecoder decoder = new DracoDecoder();
        StatusOr<Mesh> statusOr = decoder.decodeMeshFromBuffer(decoderBuffer);
        StatusChain chain = new StatusChain();
        if(statusOr.isError(chain)) throw chain.get().getRuntimeException();
        return statusOr.getValue();
    }

    public static void compareGoldenFile(String goldenFileName, EncoderBuffer buffer) {
        DecoderBuffer goldenBuffer = new DecoderBuffer();
        try(InputStream inputStream = fileToInputStream(goldenFileName)) {
            goldenBuffer.init(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        boolean contentEquals = false;
        RawPointer pointer = buffer.getData();
        RawPointer goldenPointer = goldenBuffer.getData();
        if(buffer.size() == goldenBuffer.getDecodedSize()) {
            contentEquals = PointerHelper.rawContentEquals(pointer, goldenPointer, buffer.size());
        }
        if(contentEquals) {
            TablePrinter tablePrinter = new TablePrinter();
            TableColumnPrinter actual = tablePrinter.newColumn();
            TableColumnPrinter golden = tablePrinter.newColumn();
            actual.print("Actual\n");
            golden.print("Golden\n");
            ByteTablePrinter.print(actual, pointer, buffer.size());
            ByteTablePrinter.print(golden, goldenPointer, goldenBuffer.getDecodedSize());

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            tablePrinter.print(new PrintStream(baos));
            String table = baos.toString();
            Assert.fail("Golden file content does not match the output\n" + table);
        }
    }

    public static Mesh decode(String fileName) {
        return decode(fileName, true);
    }

    public static Mesh decode(String fileName, boolean deduplicateInputValues) {
        Options options = new Options();
        options.setBool("deduplicate_input_values", deduplicateInputValues);
        return decode(fileName, options);
    }

    public static Mesh decodeWithMetadata(String fileName) {
        Options options = new Options();
        options.setBool("use_metadata", true);
        return decode(fileName, options);
    }

    public static Mesh decodeWithPolygons(String fileName) {
        Options options = new Options();
        options.setBool("preserve_polygons", true);
        return decode(fileName, options);
    }

    public static Mesh decode(String fileName, Options options) {
        StatusChain chain = new StatusChain();

        URL url = DracoTestFileUtil.class.getClassLoader().getResource(fileName);
        if(url == null) Assert.fail("File not found: " + fileName);

        File file = new File(url.getFile());
        Mesh mesh = new Mesh();

        String extension = fileName.substring(fileName.lastIndexOf('.') + 1);
        if(extension.equals("obj")) {
            ObjDecoder decoder = new ObjDecoder();
            decoder.setUseMetadata(options.getBool("use_metadata", false));
            decoder.setPreservePolygons(options.getBool("preserve_polygons"));
            decoder.setDeduplicateInputValues(options.getBool("deduplicate_input_values"));

            if(decoder.decodeFromFile(file, mesh).isError(chain)) StatusAssert.fail(chain.get());
            return mesh;
        }
        else if(extension.equals("ply")) {
            throw new UnsupportedOperationException("TODO");
        }
        else {
            return decodeDraco(fileName);
        }
    }

    public static void testDecoding(String fileName) {
        Mesh mesh = decode(fileName);
        Assert.assertTrue(mesh.getNumFaces() > 0);

        Mesh pointCloud = decode(fileName);
        Assert.assertTrue(pointCloud.getNumPoints() > 0);
    }
}
