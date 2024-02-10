package com.mndk.bteterrarenderer.mixin.mcconnector.client;

import com.mndk.bteterrarenderer.mcconnector.client.MinecraftClientManager;
import com.mndk.bteterrarenderer.mcconnector.wrapper.TextWrapper;
import lombok.experimental.UtilityClass;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
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
            return MinecraftClient.IS_SYSTEM_MAC;
        }

        public double getPlayerRotationYaw() {
            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            return player != null ? player.getYaw() : 0;
        }

        public void sendTextComponentToChat(TextWrapper textComponent) {
            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            if(player == null) return;
            player.sendMessage(textComponent.get(), false);
        }

        public void playClickSound() {
            MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(
                    SoundEvents.UI_BUTTON_CLICK, 1.0f
            ));
        }
    };}

}
