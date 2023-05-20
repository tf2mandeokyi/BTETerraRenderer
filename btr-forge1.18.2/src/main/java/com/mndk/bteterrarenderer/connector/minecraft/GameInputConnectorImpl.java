package com.mndk.bteterrarenderer.connector.minecraft;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;

public class GameInputConnectorImpl implements GameInputConnector {
    @Override
    public boolean isOnMac() {
        return Minecraft.ON_OSX;
    }

    @Override
    public boolean isKeyDown(InputKey key) {
        return InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), key.glfwKeyCode);
    }

    @Override
    public String getClipboardContent() {
        return Minecraft.getInstance().keyboardHandler.getClipboard();
    }

    @Override
    public void setClipboardContent(String content) {
        Minecraft.getInstance().keyboardHandler.setClipboard(content);
    }
}
