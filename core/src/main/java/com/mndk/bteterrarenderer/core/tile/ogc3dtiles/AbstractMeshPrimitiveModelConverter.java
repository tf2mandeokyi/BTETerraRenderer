package com.mndk.bteterrarenderer.core.tile.ogc3dtiles;

import com.mndk.bteterrarenderer.core.graphics.PreBakedModel;
import com.mndk.bteterrarenderer.util.IOUtil;
import com.mndk.bteterrarenderer.util.Loggers;
import com.mndk.bteterrarenderer.dep.terraplusplus.projection.GeographicProjection;
import com.mndk.bteterrarenderer.dep.terraplusplus.projection.OutOfProjectionBoundsException;
import com.mndk.bteterrarenderer.mcconnector.util.math.McCoord;
import com.mndk.bteterrarenderer.ogc3dtiles.math.Cartesian3f;
import com.mndk.bteterrarenderer.ogc3dtiles.math.Spheroid3;
import com.mndk.bteterrarenderer.ogc3dtiles.math.SpheroidCoordinatesConverter;
import com.mndk.bteterrarenderer.ogc3dtiles.math.matrix.Matrix4f;
import de.javagl.jgltf.model.ImageModel;
import de.javagl.jgltf.model.MaterialModel;
import de.javagl.jgltf.model.TextureModel;
import de.javagl.jgltf.model.v1.MaterialModelV1;
import de.javagl.jgltf.model.v2.MaterialModelV2;
import lombok.RequiredArgsConstructor;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

@RequiredArgsConstructor
public abstract class AbstractMeshPrimitiveModelConverter {

    private final Context context;

    protected abstract PreBakedModel convert() throws Exception;

    protected McCoord transformEarthCoordToGame(Cartesian3f earthCartesian) throws OutOfProjectionBoundsException {
        return context.transformEarthCoordToGame(earthCartesian);
    }

    public static BufferedImage readMaterialModel(MaterialModel materialModel) {
        BufferedImage image = null;
        if (materialModel instanceof MaterialModelV1) {
            throw new UnsupportedOperationException("material model v1 not supported");
        } else if (materialModel instanceof MaterialModelV2) {
            MaterialModelV2 materialModelV2 = (MaterialModelV2) materialModel;

            TextureModel textureModel = materialModelV2.getBaseColorTexture();
            if (textureModel != null) image = readImageModel(textureModel.getImageModel());
            // TODO: read mag/minFilter, wrapS/T from texture
            // TODO: read emissive, normal, occlusion, and roughness texture from material
        }
        return image;
    }

    public static BufferedImage readImageModel(ImageModel imageModel) {
        try {
            ByteBuffer byteBuffer = imageModel.getImageData();
            byte[] data = IOUtil.readAllBytes(byteBuffer);
            InputStream stream = new ByteArrayInputStream(data);

            // TODO: Add compressedImage3DTiles extension

            return ImageIO.read(stream);
        } catch (IOException e) {
            Loggers.get().error("Could not read image model", e);
            return null;
        }
    }

    @RequiredArgsConstructor
    public static class Context {

        private final Matrix4f transform;
        private final GeographicProjection projection;
        private final SpheroidCoordinatesConverter coordConverter;

        public McCoord transformEarthCoordToGame(Cartesian3f earthCoordinate) throws OutOfProjectionBoundsException {
            Cartesian3f transformed = earthCoordinate.transform(this.transform);

            Spheroid3 s3 = coordConverter.toSpheroid(transformed);
            double[] posXY = this.projection.fromGeo(s3.getLongitudeDegrees(), s3.getLatitudeDegrees());
            return new McCoord(posXY[0], (float) s3.getHeight(), posXY[1]);
        }

    }
}
