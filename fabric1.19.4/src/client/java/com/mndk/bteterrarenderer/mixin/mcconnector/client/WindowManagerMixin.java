package com.mndk.bteterrarenderer.mixin.mcconnector.client;

import com.mndk.bteterrarenderer.mcconnector.client.WindowManager;
import lombok.experimental.UtilityClass;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@UtilityClass
@Mixin(value = WindowManager.class, remap = false)
public class WindowManagerMixin {

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public int getPixelWidth() {
        return MinecraftClient.getInstance().getWindow().getWidth();
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public int getPixelHeight() {
        return MinecraftClient.getInstance().getWindow().getHeight();
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public int getScaledWidth() {
        return MinecraftClient.getInstance().getWindow().getScaledWidth();
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public int getScaledHeight() {
        return MinecraftClient.getInstance().getWindow().getScaledHeight();
    }

}
