package com.mndk.bteterrarenderer.mixin.mcconnector.gui;

import com.mndk.bteterrarenderer.mcconnector.gui.RawGuiManager;
import com.mndk.bteterrarenderer.mcconnector.gui.component.AbstractGuiScreenCopy;
import com.mndk.bteterrarenderer.mod.mcconnector.gui.AbstractGuiScreenImpl;
import lombok.experimental.UtilityClass;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@UtilityClass
@Mixin(value = RawGuiManager.class, remap = false)
public class RawGuiManagerMixin {

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    private static RawGuiManager makeInstance() { return new RawGuiManager() {
        public void displayGuiScreen(AbstractGuiScreenCopy gui) {
            MinecraftClient.getInstance().setScreen(new AbstractGuiScreenImpl(gui));
        }
    };}
}
