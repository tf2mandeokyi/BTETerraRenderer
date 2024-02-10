package com.mndk.bteterrarenderer.mixin.mcconnector.client;

import com.mndk.bteterrarenderer.mcconnector.client.MinecraftWindowManager;
import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@UtilityClass
@Mixin(value = MinecraftWindowManager.class, remap = false)
public class MinecraftWindowManagerMixin {

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    private static MinecraftWindowManager makeInstance() { return new MinecraftWindowManager() {
        public int getPixelWidth() { return Minecraft.getMinecraft().displayWidth; }
        public int getPixelHeight() { return Minecraft.getMinecraft().displayHeight; }
        public int getScaledWidth() { return new ScaledResolution(Minecraft.getMinecraft()).getScaledWidth(); }
        public int getScaledHeight() { return new ScaledResolution(Minecraft.getMinecraft()).getScaledHeight(); }
    };}

}
