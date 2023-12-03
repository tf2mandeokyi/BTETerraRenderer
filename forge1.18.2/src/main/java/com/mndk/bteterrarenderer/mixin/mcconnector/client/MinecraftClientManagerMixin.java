package com.mndk.bteterrarenderer.mixin.mcconnector.client;

import com.mndk.bteterrarenderer.mcconnector.client.MinecraftClientManager;
import lombok.experimental.UtilityClass;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@UtilityClass
@Mixin(value = MinecraftClientManager.class, remap = false)
public class MinecraftClientManagerMixin {

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public boolean isOnMac() {
        return Minecraft.ON_OSX;
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public double getPlayerRotationYaw() {
        LocalPlayer player = Minecraft.getInstance().player;
        return player != null ? player.getYRot() : 0;
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public void sendTextComponentToChat(Object textComponent) {
        LocalPlayer player = Minecraft.getInstance().player;
        if(player == null) return;
        player.sendMessage((Component) textComponent, Util.NIL_UUID);
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public void playClickSound() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(
                SoundEvents.UI_BUTTON_CLICK, 1.0f
        ));
    }

//    /** @author m4ndeokyi
//     *  @reason mixin overwrite */
//    @Overwrite
//    public int chatOpenKeyCode() {
//        return Minecraft.getInstance().options.keyChat.getKey().getValue();
//    }
//
//    /** @author m4ndeokyi
//     *  @reason mixin overwrite */
//    @Overwrite
//    public int commandOpenKeyCode() {
//        return Minecraft.getInstance().options.keyCommand.getKey().getValue();
//    }
}
