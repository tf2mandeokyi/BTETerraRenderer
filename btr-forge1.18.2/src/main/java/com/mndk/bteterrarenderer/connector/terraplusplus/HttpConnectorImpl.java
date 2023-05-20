package com.mndk.bteterrarenderer.connector.terraplusplus;

import com.mndk.bteterrarenderer.connector.terraplusplus.http.Http;
import io.netty.buffer.ByteBufInputStream;

import java.io.InputStream;
import java.util.concurrent.ExecutionException;

public class HttpConnectorImpl implements HttpConnector {
    public InputStream download(String url) throws ExecutionException, InterruptedException {
        return new ByteBufInputStream(Http.get(url).get());
    }
}
