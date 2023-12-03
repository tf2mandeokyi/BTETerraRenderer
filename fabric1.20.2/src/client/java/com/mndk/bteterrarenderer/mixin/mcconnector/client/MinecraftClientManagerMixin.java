package com.mndk.bteterrarenderer.mixin.mcconnector.client;

import com.mndk.bteterrarenderer.mcconnector.client.MinecraftClientManager;
import lombok.experimental.UtilityClass;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@UtilityClass
@Mixin(value = MinecraftClientManager.class, remap = false)
public class MinecraftClientManagerMixin {

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public boolean isOnMac() {
        return MinecraftClient.IS_SYSTEM_MAC;
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public double getPlayerRotationYaw() {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        return player != null ? player.getYaw() : 0;
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public void sendTextComponentToChat(Object textComponent) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if(player == null) return;
        player.sendMessage((Text) textComponent, false);
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public void playClickSound() {
        MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(
                SoundEvents.UI_BUTTON_CLICK, 1.0f
        ));
    }

//    /** @author m4ndeokyi
//     *  @reason mixin overwrite */
//    @Overwrite
//    public int chatOpenKeyCode() {
//        return MinecraftClient.getInstance().options.chatKey.boundKey.getCode();
//    }
//
//    /** @author m4ndeokyi
//     *  @reason mixin overwrite */
//    @Overwrite
//    public int commandOpenKeyCode() {
//        return MinecraftClient.getInstance().options.commandKey.boundKey.getCode();
//    }
}
