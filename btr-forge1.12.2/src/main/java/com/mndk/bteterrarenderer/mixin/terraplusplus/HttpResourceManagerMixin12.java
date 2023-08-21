package com.mndk.bteterrarenderer.mixin.terraplusplus;

import com.mndk.bteterrarenderer.dep.terraplusplus.HttpResourceManager;
import io.netty.buffer.ByteBufInputStream;
import lombok.experimental.UtilityClass;
import net.buildtheearth.terraplusplus.util.http.Http;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.io.InputStream;
import java.util.concurrent.ExecutionException;

@UtilityClass
@Mixin(value = HttpResourceManager.class, remap = false)
public class HttpResourceManagerMixin12 {
    @Overwrite
    public InputStream download(String url) throws ExecutionException, InterruptedException {
        return new ByteBufInputStream(Http.get(url).get());
    }
}
