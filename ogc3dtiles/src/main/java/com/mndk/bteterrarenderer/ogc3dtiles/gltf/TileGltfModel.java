package com.mndk.bteterrarenderer.ogc3dtiles.gltf;

import com.mndk.bteterrarenderer.ogc3dtiles.TileData;
import com.mndk.bteterrarenderer.ogc3dtiles.TileDataFormat;
import de.javagl.jgltf.model.GltfModel;
import de.javagl.jgltf.model.io.*;
import de.javagl.jgltf.model.io.v1.GltfAssetV1;
import de.javagl.jgltf.model.io.v2.GltfAssetV2;
import de.javagl.jgltf.model.v1.GltfModelV1;
import de.javagl.jgltf.model.v2.GltfModelCreatorV2;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import lombok.Getter;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

@Getter
public class TileGltfModel extends TileData {
    private static final Consumer<? super JsonError> JSON_ERROR_CONSUMER = JsonErrorConsumers.createLogging();

    @Nullable
    private final String copyright;
    private final GltfModel instance;

    public TileGltfModel(@Nullable String copyright, GltfModel instance) {
        super(TileDataFormat.GLTF);
        this.copyright = copyright;
        this.instance = instance;
    }

    public static TileGltfModel from(ByteBuf buf) throws IOException {
        String copyright;
        GltfModel model;
        try (InputStream gltfInputStream = new ByteBufInputStream(buf)) {
            GltfAssetReader gltfAssetReader = new GltfAssetReader();
            gltfAssetReader.setJsonErrorConsumer(JSON_ERROR_CONSUMER);
            GltfAsset gltfAsset = gltfAssetReader.readWithoutReferences(gltfInputStream);
            if (gltfAsset instanceof GltfAssetV1) {
                GltfAssetV1 gltfAssetV1 = (GltfAssetV1) gltfAsset;
                copyright = gltfAssetV1.getGltf().getAsset().getCopyright();
                model = new GltfModelV1(gltfAssetV1);
            } else if (gltfAsset instanceof GltfAssetV2) {
                GltfAssetV2 gltfAssetV2 = (GltfAssetV2) gltfAsset;
                copyright = gltfAssetV2.getGltf().getAsset().getCopyright();
                model = GltfModelCreatorV2.create(gltfAssetV2);
            } else {
                throw new IOException("The glTF asset has an unknown version: " + gltfAsset);
            }
        }
        return new TileGltfModel(copyright, model);
    }

    @Nullable
    @Override
    public GltfModel getGltfModelInstance() {
        return this.getInstance();
    }
}
