package com.mndk.bteterrarenderer.ogc3dtiles;

import com.mndk.bteterrarenderer.core.BTETerraRendererConstants;
import com.mndk.bteterrarenderer.core.util.BTRUtil;
import com.mndk.bteterrarenderer.core.util.IOUtil;
import com.mndk.bteterrarenderer.ogc3dtiles.b3dm.Batched3DModel;
import com.mndk.bteterrarenderer.ogc3dtiles.gltf.TileGltfModel;
import com.mndk.bteterrarenderer.ogc3dtiles.tile.Tileset;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.io.InputStream;

@UtilityClass
public class TileResourceManager {

    private static final byte[] B3DM_START = new byte[] { 'b', '3', 'd', 'm' };
    private static final byte[] I3DM_START = new byte[] { 'i', '3', 'd', 'm' };
    private static final byte[] GLTF_START = new byte[] { 'g', 'l', 'T', 'F' };
    private static final byte[] UTF8_BOM = new byte[] { (byte) 0xef, (byte) 0xbb, (byte) 0xbf };

    /**
     * @param stream The stream
     * @return The data
     * @throws IOException If something goes wrong while fetching the data
     */
    public TileData parse(InputStream stream) throws IOException {

        byte[] byteArray = IOUtil.readAllBytes(stream);

        TileData result;
        if(BTRUtil.arrayStartsWith(byteArray, B3DM_START)) {
            ByteBuf buf = Unpooled.wrappedBuffer(byteArray);
            result = Batched3DModel.from(buf);
        }
        else if(BTRUtil.arrayStartsWith(byteArray, I3DM_START)) {
            throw new UnsupportedOperationException("i3dm not yet implemented");
        }
        else if(BTRUtil.arrayStartsWith(byteArray, GLTF_START)) {
            ByteBuf buf = Unpooled.wrappedBuffer(byteArray);
            result = TileGltfModel.from(buf);
        }
        else if (BTRUtil.arrayStartsWith(byteArray, UTF8_BOM)) {
            ByteBuf buf = Unpooled.wrappedBuffer(byteArray, 3, byteArray.length - 3);
            result = BTETerraRendererConstants.JSON_MAPPER
                    .readValue((InputStream) new ByteBufInputStream(buf), Tileset.class);
        }
        else {
            String jsonTry = new String(byteArray);
            result = BTETerraRendererConstants.JSON_MAPPER.readValue(jsonTry, Tileset.class);
        }

        return result;
    }

}
