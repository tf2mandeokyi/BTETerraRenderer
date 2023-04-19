package com.mndk.bteterrarenderer.connector.minecraft;

import com.mndk.bteterrarenderer.BTETerraRendererConstants;
import com.mndk.bteterrarenderer.connector.ConnectorImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentString;

@ConnectorImpl
@SuppressWarnings("unused")
public class ErrorMessageHandlerImpl implements ErrorMessageHandler {

    public void sendToClient(String message) {
        if(Minecraft.getMinecraft().player != null) {
            String componentString = "§c[" + BTETerraRendererConstants.NAME + "] " + message;
            Minecraft.getMinecraft().player.sendMessage(new TextComponentString(componentString));
        }
        BTETerraRendererConstants.LOGGER.error(message);
    }

    public void sendToClient(String message, Throwable t) {
        sendToClient(message);
        sendToClient("Reason: " + t.getMessage());
        t.printStackTrace();
    }

}
