package com.mndk.bteterrarenderer.chat;

import com.mndk.bteterrarenderer.BTETerraRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentString;

public class ErrorMessageHandler {

    public static void sendToClient(String message) {
        if(Minecraft.getMinecraft().player != null) {
            Minecraft.getMinecraft().player.sendMessage(new TextComponentString("§c[BTETerraRenderer] " + message));
        }
        BTETerraRenderer.logger.error(message);
    }

    public static void sendToClient(String message, Throwable t) {
        sendToClient(message);
        sendToClient("Reason: " + t.getMessage());
        t.printStackTrace();
    }

}
