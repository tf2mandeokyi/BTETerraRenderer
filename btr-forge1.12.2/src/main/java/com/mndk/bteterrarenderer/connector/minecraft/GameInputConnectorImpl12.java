package com.mndk.bteterrarenderer.connector.minecraft;

import com.mndk.bteterrarenderer.connector.ConnectorImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;

@ConnectorImpl
@SuppressWarnings("unused")
public class GameInputConnectorImpl12 implements GameInputConnector {
    @Override
    public boolean isOnMac() {
        return Minecraft.IS_RUNNING_ON_MAC;
    }

    @Override
    public boolean isKeyDown(InputKey key) {
        return Keyboard.isKeyDown(key.keyboardCode);
    }

    @Override
    public String getClipboardContent() {
        return GuiScreen.getClipboardString();
    }

    @Override
    public void setClipboardContent(String content) {
        GuiScreen.setClipboardString(content);
    }
}
