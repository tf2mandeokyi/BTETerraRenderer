package com.mndk.bteterrarenderer.connector.minecraft;

import com.mndk.bteterrarenderer.connector.ConnectorImpl;
import net.minecraft.client.Minecraft;

@ConnectorImpl
@SuppressWarnings("unused")
public class KeyBindingsConnectorImpl implements KeyBindingsConnector {
    public int chatOpenKeyCode() {
        return Minecraft.getMinecraft().gameSettings.keyBindChat.getKeyCode();
    }
    public int commandOpenKeyCode() {
        return Minecraft.getMinecraft().gameSettings.keyBindCommand.getKeyCode();
    }
}
