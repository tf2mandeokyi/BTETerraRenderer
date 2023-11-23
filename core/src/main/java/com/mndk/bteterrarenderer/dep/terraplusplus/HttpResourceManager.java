package com.mndk.bteterrarenderer.dep.terraplusplus;

import com.mndk.bteterrarenderer.dep.terraplusplus.http.Http;
import io.netty.buffer.ByteBufInputStream;
import lombok.experimental.UtilityClass;

import java.io.InputStream;
import java.util.concurrent.ExecutionException;

@UtilityClass
public class HttpResourceManager {
    public InputStream download(String url) throws ExecutionException, InterruptedException {
        return new ByteBufInputStream(Http.get(url).get());
    }
}
