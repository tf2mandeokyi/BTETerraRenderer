package com.mndk.bteterrarenderer.connector.minecraft;

import com.mndk.bteterrarenderer.connector.ConnectorImpl;
import net.minecraft.client.Minecraft;

@ConnectorImpl
public class GameSettingsConnectorImpl implements GameSettingsConnector {
    public int getKeyBindChatCode() {
        return Minecraft.getMinecraft().gameSettings.keyBindChat.getKeyCode();
    }
    public int getKeyBindCommandCode() {
        return Minecraft.getMinecraft().gameSettings.keyBindCommand.getKeyCode();
    }
}
