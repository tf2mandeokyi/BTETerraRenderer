package com.mndk.bteterrarenderer.mixin.minecraft;

import com.mndk.bteterrarenderer.core.BTETerraRendererConstants;
import com.mndk.bteterrarenderer.core.util.minecraft.MinecraftClientManager;
import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.text.TextComponentString;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@UtilityClass
@Mixin(value = MinecraftClientManager.class, remap = false)
public class MinecraftClientManagerMixin12 {

    @Overwrite
    public double getPlayerRotationYaw() {
        return Minecraft.getMinecraft().player.rotationYaw;
    }

    @Overwrite
    public void sendErrorMessageToChat(String message) {
        if(Minecraft.getMinecraft().player != null) {
            String componentString = "§c[" + BTETerraRendererConstants.NAME + "] " + message;
            Minecraft.getMinecraft().player.sendMessage(new TextComponentString(componentString));
        }
        BTETerraRendererConstants.LOGGER.error(message);
    }

    @Overwrite
    public void sendErrorMessageToChat(String message, Throwable t) {
        sendErrorMessageToChat(message);
        sendErrorMessageToChat("Reason: " + t.getMessage());
        BTETerraRendererConstants.LOGGER.error(message, t);
    }

    @Overwrite
    public void playClickSound() {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(
                SoundEvents.UI_BUTTON_CLICK, 1.0F
        ));
    }

    @Overwrite
    public int chatOpenKeyCode() {
        return Minecraft.getMinecraft().gameSettings.keyBindChat.getKeyCode();
    }

    @Overwrite
    public int commandOpenKeyCode() {
        return Minecraft.getMinecraft().gameSettings.keyBindCommand.getKeyCode();
    }
}
