package com.mndk.bteterrarenderer.ogc3dtiles.gltf;

import com.mndk.bteterrarenderer.ogc3dtiles.TileData;
import com.mndk.bteterrarenderer.ogc3dtiles.TileDataFormat;
import de.javagl.jgltf.model.GltfModel;
import de.javagl.jgltf.model.io.GltfModelReader;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import lombok.Getter;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;

@Getter
public class TileGltfModel extends TileData {
    public static final GltfModelReader GLTF_MODEL_READER = new GltfModelReader();

    private final GltfModel instance;

    public TileGltfModel(GltfModel instance) {
        super(TileDataFormat.GLTF);
        this.instance = instance;
    }

    public static TileGltfModel from(ByteBuf buf) throws IOException {
        InputStream gltfInputStream = new ByteBufInputStream(buf);
        GltfModel gltfModel = GLTF_MODEL_READER.readWithoutReferences(gltfInputStream);

        gltfInputStream.close();
        return new TileGltfModel(gltfModel);
    }

    @Nullable
    @Override
    public GltfModel getGltfModelInstance() {
        return this.getInstance();
    }
}
