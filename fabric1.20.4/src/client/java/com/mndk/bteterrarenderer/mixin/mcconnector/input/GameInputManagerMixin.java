package com.mndk.bteterrarenderer.mixin.mcconnector.input;

import com.mndk.bteterrarenderer.mcconnector.input.GameInputManager;
import com.mndk.bteterrarenderer.mcconnector.input.InputKey;
import lombok.experimental.UtilityClass;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@UtilityClass
@Mixin(value = GameInputManager.class, remap = false)
public class GameInputManagerMixin {

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public boolean isKeyDown(InputKey key) {
        return InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), key.glfwKeyCode);
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public String getClipboardContent() {
        return MinecraftClient.getInstance().keyboard.getClipboard();
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public void setClipboardContent(String content) {
        MinecraftClient.getInstance().keyboard.setClipboard(content);
    }
}
