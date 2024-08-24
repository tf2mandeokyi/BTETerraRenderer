package com.mndk.bteterrarenderer.core.tile.ogc3dtiles;

import com.mndk.bteterrarenderer.core.graphics.PreBakedModel;
import com.mndk.bteterrarenderer.core.util.Loggers;
import com.mndk.bteterrarenderer.dep.terraplusplus.projection.OutOfProjectionBoundsException;
import com.mndk.bteterrarenderer.ogc3dtiles.math.Cartesian3;
import de.javagl.jgltf.model.ImageModel;
import de.javagl.jgltf.model.MaterialModel;
import de.javagl.jgltf.model.TextureModel;
import de.javagl.jgltf.model.v1.MaterialModelV1;
import de.javagl.jgltf.model.v2.MaterialModelV2;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import lombok.RequiredArgsConstructor;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

@RequiredArgsConstructor
abstract class AbstractMeshPrimitiveModelConverter {

    private final SingleGltfModelParsingContext context;

    protected abstract PreBakedModel convert() throws Exception;

    protected Cartesian3 transformEarthCoordToGame(Cartesian3 earthCartesian) throws OutOfProjectionBoundsException {
        return context.transformEarthCoordToGame(earthCartesian);
    }

    protected static BufferedImage readMaterialModel(MaterialModel materialModel) {
        BufferedImage image = null;
        if(materialModel instanceof MaterialModelV1) {
            throw new UnsupportedOperationException("material model v1 not supported");
        } else if(materialModel instanceof MaterialModelV2) {
            MaterialModelV2 materialModelV2 = (MaterialModelV2) materialModel;

            TextureModel textureModel = materialModelV2.getBaseColorTexture();
            if(textureModel != null) image = readImageModel(textureModel.getImageModel());
            // TODO: read mag/minFilter, wrapS/T from texture
            // TODO: read emissive, normal, occlusion, and roughness texture from material
        }
        return image;
    }

    protected static BufferedImage readImageModel(ImageModel imageModel) {
        try {
            ByteBuffer byteBuffer = imageModel.getImageData();
            ByteBuf nettyBuf = Unpooled.copiedBuffer(byteBuffer);
            InputStream stream = new ByteBufInputStream(nettyBuf);

            // TODO: Add compressedImage3DTiles extension

            return ImageIO.read(stream);
        } catch(IOException e) {
            Loggers.get().error("Could not read image model", e);
            return null;
        }
    }
}
