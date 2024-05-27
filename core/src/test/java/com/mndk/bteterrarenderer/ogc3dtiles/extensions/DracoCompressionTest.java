package com.mndk.bteterrarenderer.ogc3dtiles.extensions;

import com.mndk.bteterrarenderer.core.projection.Projections;
import com.mndk.bteterrarenderer.core.tile.ogc3dtiles.GltfModelConverter;
import com.mndk.bteterrarenderer.ogc3dtiles.TileData;
import com.mndk.bteterrarenderer.ogc3dtiles.TileResourceManager;
import com.mndk.bteterrarenderer.ogc3dtiles.math.matrix.Matrix4;
import de.javagl.jgltf.model.GltfModel;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

public class DracoCompressionTest {

    @Test
    public void lfashiebcefhlcbhykuc() throws IOException {
        try(InputStream stream = getClass().getClassLoader().getResourceAsStream("model_test.glb")) {
            if (stream == null) throw new IOException("model_test.glb not found");

            TileData data = TileResourceManager.parse(stream);
            GltfModel model = data.getGltfModelInstance();
            GltfModelConverter.convertModel(model, Matrix4.IDENTITY, Projections.BTE);
        }
    }
}
