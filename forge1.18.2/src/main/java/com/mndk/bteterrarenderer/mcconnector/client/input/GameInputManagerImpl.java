package com.mndk.bteterrarenderer.mcconnector.client.input;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.ClientRegistry;

public class GameInputManagerImpl implements GameInputManager {

    public boolean isKeyDown(InputKey key) {
        return InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), key.glfwKeyCode);
    }

    public IKeyBinding registerInternal(String description, InputKey key, String category) {
        KeyMapping keyMapping = new KeyMapping(description, key.glfwKeyCode, category);
        ClientRegistry.registerKeyBinding(keyMapping);
        return keyMapping::consumeClick;
    }

    public String getClipboardContent() {
        return Minecraft.getInstance().keyboardHandler.getClipboard();
    }

    public void setClipboardContent(String content) {
        Minecraft.getInstance().keyboardHandler.setClipboard(content);
    }
}
