package com.mndk.bteterrarenderer.mixin.core.network;

import com.mndk.bteterrarenderer.core.network.HttpResourceManager;
import io.netty.buffer.ByteBuf;
import lombok.experimental.UtilityClass;
import net.buildtheearth.terraplusplus.util.http.Http;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.concurrent.ExecutionException;

@UtilityClass
@Mixin(value = HttpResourceManager.class, remap = false)
public class HttpResourceManagerMixin {
    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public ByteBuf download(String url) throws ExecutionException, InterruptedException {
        return Http.get(url).get();
    }
}
