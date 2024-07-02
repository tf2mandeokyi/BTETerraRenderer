package com.mndk.bteterrarenderer.draco.core;

import com.mndk.bteterrarenderer.draco.io.ObjDecoder;
import com.mndk.bteterrarenderer.draco.mesh.Mesh;

import java.io.File;
import java.net.URL;

public class MeshIOTest {

    public static StatusOr<Mesh> readObjFromFile(String fileName) {
        return readObjFromFile(fileName, new Options());
    }

    public static StatusOr<Mesh> readObjFromFile(String fileName, Options options) {
        StatusChain chain = new StatusChain();

        ObjDecoder decoder = new ObjDecoder();
        decoder.setUseMetadata(options.getBool("use_metadata", false));
        decoder.setPreservePolygons(options.getBool("preserve_polygons"));

        URL url = MeshIOTest.class.getClassLoader().getResource(fileName);
        if(url == null) return StatusOr.ioError("File not found: " + fileName);

        File file = new File(url.getFile());
        Mesh mesh = new Mesh();
        if(decoder.decodeFromFile(file, mesh).isError(chain)) return StatusOr.error(chain);
        return StatusOr.ok(mesh);
    }

}
