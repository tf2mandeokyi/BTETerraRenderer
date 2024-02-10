package com.mndk.bteterrarenderer.mixin.mcconnector.client;

import com.mndk.bteterrarenderer.mcconnector.client.MinecraftClientManager;
import com.mndk.bteterrarenderer.mcconnector.wrapper.TextWrapper;
import lombok.experimental.UtilityClass;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
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
            return Minecraft.ON_OSX;
        }

        public double getPlayerRotationYaw() {
            LocalPlayer player = Minecraft.getInstance().player;
            return player != null ? player.getYRot() : 0;
        }

        public void sendTextComponentToChat(TextWrapper textComponent) {
            LocalPlayer player = Minecraft.getInstance().player;
            if(player == null) return;
            player.sendMessage(textComponent.get(), Util.NIL_UUID);
        }

        public void playClickSound() {
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(
                    SoundEvents.UI_BUTTON_CLICK, 1.0f
            ));
        }
    };}

}
