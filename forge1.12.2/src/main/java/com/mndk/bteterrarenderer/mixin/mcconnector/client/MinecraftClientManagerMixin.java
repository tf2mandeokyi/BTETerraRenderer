package com.mndk.bteterrarenderer.mixin.mcconnector.client;

import com.mndk.bteterrarenderer.mcconnector.client.MinecraftClientManager;
import com.mndk.bteterrarenderer.mcconnector.wrapper.TextWrapper;
import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.init.SoundEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@UtilityClass
@Mixin(value = MinecraftClientManager.class, remap = false)
public class MinecraftClientManagerMixin {

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    private static MinecraftClientManager makeInstance() { return new MinecraftClientManager() {
        public boolean isOnMac() {
            return Minecraft.IS_RUNNING_ON_MAC;
        }

        public double getPlayerRotationYaw() {
            return Minecraft.getMinecraft().player.rotationYaw;
        }

        public void sendTextComponentToChat(TextWrapper textComponent) {
            EntityPlayerSP player = Minecraft.getMinecraft().player;
            if(player == null) return;
            player.sendMessage(textComponent.get());
        }

        public void playClickSound() {
            Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(
                    SoundEvents.UI_BUTTON_CLICK, 1.0F
            ));
        }
    };}

}
