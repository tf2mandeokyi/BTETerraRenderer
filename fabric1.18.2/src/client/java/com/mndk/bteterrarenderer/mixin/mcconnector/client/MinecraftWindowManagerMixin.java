package com.mndk.bteterrarenderer.mixin.mcconnector.client;

import com.mndk.bteterrarenderer.mcconnector.client.MinecraftWindowManager;
import lombok.experimental.UtilityClass;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@UtilityClass
@Mixin(value = MinecraftWindowManager.class, remap = false)
public class MinecraftWindowManagerMixin {

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    private static MinecraftWindowManager makeInstance() { return new MinecraftWindowManager() {
        public int getPixelWidth() { return MinecraftClient.getInstance().getWindow().getWidth(); }
        public int getPixelHeight() { return MinecraftClient.getInstance().getWindow().getHeight(); }
        public int getScaledWidth() { return MinecraftClient.getInstance().getWindow().getScaledWidth(); }
        public int getScaledHeight() { return MinecraftClient.getInstance().getWindow().getScaledHeight(); }
    };}

}
