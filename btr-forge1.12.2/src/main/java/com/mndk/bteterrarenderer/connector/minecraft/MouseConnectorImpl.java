package com.mndk.bteterrarenderer.connector.minecraft;

import com.mndk.bteterrarenderer.connector.ConnectorImpl;
import org.lwjgl.input.Mouse;

@ConnectorImpl
public class MouseConnectorImpl implements MouseConnector {
    public int getEventX() {
        return Mouse.getEventX();
    }
    public int getEventDWheel() {
        return Mouse.getEventDWheel();
    }
}