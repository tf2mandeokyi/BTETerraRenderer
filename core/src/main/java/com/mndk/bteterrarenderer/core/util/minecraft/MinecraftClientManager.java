package com.mndk.bteterrarenderer.core.util.minecraft;

import com.mndk.bteterrarenderer.core.BTETerraRendererConstants;
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
        sendErrorMessageToChat(message);
        sendErrorMessageToChat("Reason: " + t.getMessage());
        BTETerraRendererConstants.LOGGER.error(message, t);
    }

    public void playClickSound() {
        MixinUtil.notOverwritten();
    }

//    /*
//     * Consider changing these to enum type
//     */
//    public boolean matchesChatOpenKeyCode(int keyCode) {
//        return MixinUtil.notOverwritten(keyCode);
//    }
//    public boolean matchesCommandOpenKeyCode(int keyCode) {
//        return MixinUtil.notOverwritten(keyCode);
//    }
}