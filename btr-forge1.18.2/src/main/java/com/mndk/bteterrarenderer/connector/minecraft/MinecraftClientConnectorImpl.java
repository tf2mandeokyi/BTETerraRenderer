package com.mndk.bteterrarenderer.connector.minecraft;

import com.mndk.bteterrarenderer.BTETerraRendererConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.sounds.SoundEvents;

public class MinecraftClientConnectorImpl implements MinecraftClientConnector {

    public double getPlayerRotationYaw() {
        LocalPlayer player = Minecraft.getInstance().player;
        return player != null ? player.getYRot() : 0;
    }

    public void sendErrorMessageToChat(String message) {
        LocalPlayer player = Minecraft.getInstance().player;
        if(player != null) {
            String componentString = "§c[" + BTETerraRendererConstants.NAME + "] " + message;
            player.sendMessage(new TextComponent(componentString), Util.NIL_UUID);
        }
        BTETerraRendererConstants.LOGGER.error(message);
    }
    public void sendErrorMessageToChat(String message, Throwable t) {
        this.sendErrorMessageToChat(message);
        this.sendErrorMessageToChat("Reason: " + t.getMessage());
        t.printStackTrace();
    }

    public void playClickSound() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(
                SoundEvents.UI_BUTTON_CLICK, 1.0f
        ));
    }

    public int chatOpenKeyCode() {
        return Minecraft.getInstance().options.keyChat.getKey().getValue();
    }
    public int commandOpenKeyCode() {
        return Minecraft.getInstance().options.keyCommand.getKey().getValue();
    }
}
