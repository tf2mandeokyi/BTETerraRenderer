package com.mndk.bteterrarenderer.core.util.minecraft;

import com.mndk.bteterrarenderer.core.util.mixin.MixinUtil;
import lombok.experimental.UtilityClass;

@UtilityClass
public class MinecraftClientManager {
    public double getPlayerRotationYaw() {
        return MixinUtil.notOverwritten();
    }

    public void sendErrorMessageToChat(String message) {
        MixinUtil.notOverwritten(message);
    }
    public void sendErrorMessageToChat(String message, Throwable t) {
        MixinUtil.notOverwritten(message, t);
    }

    public void playClickSound() {
        MixinUtil.notOverwritten();
    }

    /*
     * Consider changing these to enum type
     */
    public int chatOpenKeyCode() {
        return MixinUtil.notOverwritten();
    }
    public int commandOpenKeyCode() {
        return MixinUtil.notOverwritten();
    }
}
