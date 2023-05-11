package com.mndk.bteterrarenderer.connector.minecraft;

import com.mndk.bteterrarenderer.connector.ConnectorImpl;
import net.minecraft.client.Minecraft;

@ConnectorImpl
@SuppressWarnings("unused")
public class KeyBindingsConnectorImpl implements KeyBindingsConnector {
    public int chatOpenKeyCode() {
        return Minecraft.getInstance().options.keyChat.getKey().getValue();
    }
    public int commandOpenKeyCode() {
        return Minecraft.getInstance().options.keyCommand.getKey().getValue();
    }
}
