package com.mndk.bteterrarenderer.ogc3d;

import com.mndk.bteterrarenderer.BTETerraRendererConstants;
import com.mndk.bteterrarenderer.connector.terraplusplus.HttpConnector;
import com.mndk.bteterrarenderer.ogc3d.b3dm.Batched3DModel;
import com.mndk.bteterrarenderer.ogc3d.tile.Tileset;
import com.mndk.bteterrarenderer.ogc3d.util.DebugUtil;
import com.mndk.bteterrarenderer.util.BtrUtil;
import com.mndk.bteterrarenderer.util.IOUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

public class Ogc3dTileReader {

    public static OgcFileContent read(String url) throws IOException, ExecutionException, InterruptedException {
        InputStream stream = HttpConnector.INSTANCE.download(url);
        return read(stream);
    }

    public static OgcFileContent read(InputStream stream) throws IOException {
        byte[] byteArray = IOUtil.readAllBytes(stream);

        DebugUtil.printBinary(System.out, Unpooled.wrappedBuffer(Arrays.copyOfRange(byteArray, 0, 64)),
                16, 64);

        if(BtrUtil.arrayStartsWith(byteArray, new byte[] { 'b', '3', 'd', 'm' })) {
            ByteBuf buf = Unpooled.wrappedBuffer(byteArray);
            return Batched3DModel.from(buf);
        }
        if(BtrUtil.arrayStartsWith(byteArray, new byte[] { 'i', '3', 'd', 'm' })) {
            throw new UnsupportedOperationException("i3dm not yet implemented");
        }
        // Unicode BOM (byte order mark)
        if(BtrUtil.arrayStartsWith(byteArray, new byte[] { (byte) 0xef, (byte) 0xbb, (byte) 0xbf })) {
            ByteBuf buf = Unpooled.wrappedBuffer(byteArray, 3, byteArray.length - 3);
            return BTETerraRendererConstants.JSON_MAPPER.readValue((InputStream) new ByteBufInputStream(buf), Tileset.class);
        }

        String jsonTry = new String(byteArray);
        return BTETerraRendererConstants.JSON_MAPPER.readValue(jsonTry, Tileset.class);
    }

}
