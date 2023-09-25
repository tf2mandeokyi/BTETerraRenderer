package com.mndk.bteterrarenderer.mixin.minecraft;

import com.mndk.bteterrarenderer.core.util.input.GameInputManager;
import com.mndk.bteterrarenderer.core.util.input.InputKey;
import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@UtilityClass
@Mixin(value = GameInputManager.class, remap = false)
public class GameInputManagerMixin {

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public boolean isOnMac() {
        return Minecraft.IS_RUNNING_ON_MAC;
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public boolean isKeyDown(InputKey key) {
        return Keyboard.isKeyDown(key.keyboardCode);
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public String getClipboardContent() {
        return GuiScreen.getClipboardString();
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public void setClipboardContent(String content) {
        GuiScreen.setClipboardString(content);
    }
}
