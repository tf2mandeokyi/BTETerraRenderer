package com.mndk.bteterrarenderer.mixin.minecraft;

import com.mndk.bteterrarenderer.core.BTETerraRendererConstants;
import com.mndk.bteterrarenderer.core.util.minecraft.MinecraftClientManager;
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
    public double getPlayerRotationYaw() {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        return player != null ? player.getYaw() : 0;
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public void sendErrorMessageToChat(String message) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if(player != null) {
            String componentString = "§c[" + BTETerraRendererConstants.NAME + "] " + message;
            MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.of(componentString));
        }
        BTETerraRendererConstants.LOGGER.error(message);
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
