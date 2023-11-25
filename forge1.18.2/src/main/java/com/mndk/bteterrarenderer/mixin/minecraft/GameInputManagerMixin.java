package com.mndk.bteterrarenderer.mixin.minecraft;

import com.mndk.bteterrarenderer.core.input.InputKey;
import com.mndk.bteterrarenderer.core.input.GameInputManager;
import com.mojang.blaze3d.platform.InputConstants;
import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@UtilityClass
@Mixin(value = GameInputManager.class, remap = false)
public class GameInputManagerMixin {

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public boolean isKeyDown(InputKey key) {
        return InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), key.glfwKeyCode);
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public String getClipboardContent() {
        return Minecraft.getInstance().keyboardHandler.getClipboard();
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public void setClipboardContent(String content) {
        Minecraft.getInstance().keyboardHandler.setClipboard(content);
    }
}
