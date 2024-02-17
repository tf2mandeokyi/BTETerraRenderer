package com.mndk.bteterrarenderer.mixin.mcconnector.gui;

import com.mndk.bteterrarenderer.mcconnector.gui.RawGuiManager;
import com.mndk.bteterrarenderer.mcconnector.gui.screen.AbstractGuiScreenCopy;
import com.mndk.bteterrarenderer.mcconnector.wrapper.NativeGuiScreenWrapper;
import com.mndk.bteterrarenderer.mod.mcconnector.gui.AbstractGuiScreenImpl;
import lombok.experimental.UtilityClass;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import javax.annotation.Nullable;

@UtilityClass
@Mixin(value = RawGuiManager.class, remap = false)
public class RawGuiManagerMixin {

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    private RawGuiManager makeInstance() { return new RawGuiManager() {
        public NativeGuiScreenWrapper<?> newNativeChatScreen(String initialText) {
            return NativeGuiScreenWrapper.of(new ChatScreen(initialText));
        }
        public void displayGuiScreen(@Nullable AbstractGuiScreenCopy gui) {
            MinecraftClient.getInstance().setScreen(gui == null ? null : new AbstractGuiScreenImpl(gui));
        }
    };}

}
