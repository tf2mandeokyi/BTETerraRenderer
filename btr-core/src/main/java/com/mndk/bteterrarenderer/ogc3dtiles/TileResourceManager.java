package com.mndk.bteterrarenderer.ogc3dtiles;

import com.mndk.bteterrarenderer.core.BTETerraRendererConstants;
import com.mndk.bteterrarenderer.ogc3dtiles.b3dm.Batched3DModel;
import com.mndk.bteterrarenderer.ogc3dtiles.gltf.TileGltfModel;
import com.mndk.bteterrarenderer.ogc3dtiles.math.matrix.Matrix4;
import com.mndk.bteterrarenderer.ogc3dtiles.tile.Tileset;
import com.mndk.bteterrarenderer.ogc3dtiles.util.OgcDebugUtil;
import com.mndk.bteterrarenderer.core.util.BtrUtil;
import com.mndk.bteterrarenderer.core.util.IOUtil;
import com.mndk.bteterrarenderer.dep.terraplusplus.HttpResourceManager;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;

public class TileResourceManager {

    private static final byte[] B3DM_START = new byte[] { 'b', '3', 'd', 'm' };
    private static final byte[] I3DM_START = new byte[] { 'i', '3', 'd', 'm' };
    private static final byte[] GLTF_START = new byte[] { 'g', 'l', 'T', 'F' };
    private static final byte[] UTF8_BOM = new byte[] { (byte) 0xef, (byte) 0xbb, (byte) 0xbf };

    public static TileData fetch(String url) throws IOException {
        return fetch(new URL(url));
    }

    public static TileData fetch(URL url) throws IOException {
        return fetch(url, null, null);
    }

    /**
     * @param url The url
     * @param parent The parent
     * @param parentLocalTransform The parent's transform. It should be its local transform, not its true transform
     * @return The data
     * @throws IOException If something goes wrong while fetching the data
     */
    public static TileData fetch(URL url,
                                 @Nullable TileData parent,
                                 @Nullable Matrix4 parentLocalTransform) throws IOException {
        return parse(HttpResourceManager.download(url.toString()), url, parent, parentLocalTransform);
    }

    /**
     * @param stream The stream
     * @param url The url
     * @param parent The parent
     * @param parentLocalTransform The parent's transform. It should be its local transform, not its true transform
     * @return The data
     * @throws IOException If something goes wrong while fetching the data
     */
    public static TileData parse(InputStream stream,
                                 URL url,
                                 @Nullable TileData parent,
                                 @Nullable Matrix4 parentLocalTransform) throws IOException {

        byte[] byteArray = IOUtil.readAllBytes(stream);

        // TODO: Remove this
        StringBuilder sb = new StringBuilder("\n");
        OgcDebugUtil.writeBinaryTableString(sb, Unpooled.wrappedBuffer(Arrays.copyOfRange(byteArray, 0, 64)),
                16, 64);
        Constants.LOGGER.debug(sb);

        TileData result;
        if(BtrUtil.arrayStartsWith(byteArray, B3DM_START)) {
            ByteBuf buf = Unpooled.wrappedBuffer(byteArray);
            result = Batched3DModel.from(buf);
        }
        else if(BtrUtil.arrayStartsWith(byteArray, I3DM_START)) {
            throw new UnsupportedOperationException("i3dm not yet implemented");
        }
        else if(BtrUtil.arrayStartsWith(byteArray, GLTF_START)) {
            ByteBuf buf = Unpooled.wrappedBuffer(byteArray);
            result = TileGltfModel.from(buf);
        }
        else if (BtrUtil.arrayStartsWith(byteArray, UTF8_BOM)) {
            ByteBuf buf = Unpooled.wrappedBuffer(byteArray, 3, byteArray.length - 3);
            result = BTETerraRendererConstants.JSON_MAPPER
                    .readValue((InputStream) new ByteBufInputStream(buf), Tileset.class);
        }
        else {
            String jsonTry = new String(byteArray);
            result = BTETerraRendererConstants.JSON_MAPPER.readValue(jsonTry, Tileset.class);
        }

        result.setParent(parent);
        result.setParentLocalTransform(parentLocalTransform);
        result.setUrl(url);
        return result;
    }

}
