package com.mndk.bteterrarenderer.connector.minecraft;

import com.mndk.bteterrarenderer.connector.ConnectorImpl;
import net.minecraft.client.Minecraft;

@ConnectorImpl
@SuppressWarnings("unused")
public class MouseConnectorImpl /*implements MouseConnector*/ { // TODO implement this
    public int getEventX() { // TODO finish this class
        return (int) Minecraft.getInstance().mouseHandler.xpos();
    }
//    public int getEventDWheel() {
//        return (int) Minecraft.getInstance().mouseHandler.pos();
//    }
}