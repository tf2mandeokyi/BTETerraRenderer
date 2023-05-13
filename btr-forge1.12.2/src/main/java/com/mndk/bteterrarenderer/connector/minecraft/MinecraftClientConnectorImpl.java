package com.mndk.bteterrarenderer.connector.minecraft;

import com.mndk.bteterrarenderer.BTETerraRendererConstants;
import com.mndk.bteterrarenderer.connector.ConnectorImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.text.TextComponentString;
import org.lwjgl.input.Mouse;

@ConnectorImpl
@SuppressWarnings("unused")
public class MinecraftClientConnectorImpl implements MinecraftClientConnector {
    public double getPlayerRotationYaw() {
        return Minecraft.getMinecraft().player.rotationYaw;
    }

    public void sendErrorMessageToChat(String message) {
        if(Minecraft.getMinecraft().player != null) {
            String componentString = "§c[" + BTETerraRendererConstants.NAME + "] " + message;
            Minecraft.getMinecraft().player.sendMessage(new TextComponentString(componentString));
        }
        BTETerraRendererConstants.LOGGER.error(message);
    }
    public void sendErrorMessageToChat(String message, Throwable t) {
        this.sendErrorMessageToChat(message);
        this.sendErrorMessageToChat("Reason: " + t.getMessage());
        t.printStackTrace();
    }

    public void playClickSound() {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(
                SoundEvents.UI_BUTTON_CLICK, 1.0F
        ));
    }

    public int getMouseX() {
        return Mouse.getEventX();
    }
    public int getMouseDWheel() {
        return Mouse.getEventDWheel();
    }

    public int chatOpenKeyCode() {
        return Minecraft.getMinecraft().gameSettings.keyBindChat.getKeyCode();
    }
    public int commandOpenKeyCode() {
        return Minecraft.getMinecraft().gameSettings.keyBindCommand.getKeyCode();
    }
}
