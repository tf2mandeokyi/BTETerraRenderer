package com.mndk.bteterrarenderer.mcconnector.client;

import com.mndk.bteterrarenderer.mcconnector.MixinUtil;
import lombok.experimental.UtilityClass;

/**
 * Contains miscellaneous utility methods related to the game
 */
@UtilityClass
public class MinecraftClientManager {

    public boolean isOnMac() {
        return MixinUtil.notOverwritten();
    }

    public double getPlayerRotationYaw() {
        return MixinUtil.notOverwritten();
    }

    public void sendTextComponentToChat(Object textComponent) {
        MixinUtil.notOverwritten(textComponent);
    }

    public void playClickSound() {
        MixinUtil.notOverwritten();
    }
}
