package com.mndk.bteterrarenderer.connector.minecraft;

import com.mndk.bteterrarenderer.connector.ConnectorImpl;
import net.minecraft.client.Minecraft;

@ConnectorImpl
@SuppressWarnings("unused")
public class ClientPlayerConnectorImpl implements ClientPlayerConnector {
    public double getRotationYaw() {
        assert Minecraft.getInstance().player != null;
        return Minecraft.getInstance().player.getXRot();
    }
}
