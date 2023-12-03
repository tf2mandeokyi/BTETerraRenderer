package com.mndk.bteterrarenderer.mixin.mcconnector.input;

import com.mndk.bteterrarenderer.mcconnector.input.GameInputManager;
import com.mndk.bteterrarenderer.mcconnector.input.InputKey;
import lombok.experimental.UtilityClass;
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
