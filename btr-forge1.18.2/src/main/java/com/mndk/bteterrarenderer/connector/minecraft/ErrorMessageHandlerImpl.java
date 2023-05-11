package com.mndk.bteterrarenderer.connector.minecraft;

import com.mndk.bteterrarenderer.BTETerraRendererConstants;
import com.mndk.bteterrarenderer.connector.ConnectorImpl;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.TextComponent;

@ConnectorImpl
@SuppressWarnings("unused")
public class ErrorMessageHandlerImpl implements ErrorMessageHandler {

    public void sendToClient(String message) {
        LocalPlayer player = Minecraft.getInstance().player;
        if(player != null) {
            String componentString = "§c[" + BTETerraRendererConstants.NAME + "] " + message;
            player.sendMessage(new TextComponent(componentString), Util.NIL_UUID);
        }
        BTETerraRendererConstants.LOGGER.error(message);
    }

    public void sendToClient(String message, Throwable t) {
        sendToClient(message);
        sendToClient("Reason: " + t.getMessage());
        t.printStackTrace();
    }

}
