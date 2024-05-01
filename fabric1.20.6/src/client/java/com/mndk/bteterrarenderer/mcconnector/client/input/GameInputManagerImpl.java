package com.mndk.bteterrarenderer.mcconnector.client.input;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

public class GameInputManagerImpl implements GameInputManager {

    public boolean isKeyDown(InputKey key) {
        return InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), key.glfwKeyCode);
    }

    @Override
    public IKeyBinding registerInternal(String description, InputKey key, String category) {
        KeyBinding keyBinding = new KeyBinding(description, key.glfwKeyCode, category);
        KeyBindingHelper.registerKeyBinding(keyBinding);
        return keyBinding::wasPressed;
    }

    public String getClipboardContent() {
        return MinecraftClient.getInstance().keyboard.getClipboard();
    }

    public void setClipboardContent(String content) {
        MinecraftClient.getInstance().keyboard.setClipboard(content);
    }
}
