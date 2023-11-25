package com.mndk.bteterrarenderer.core.util.minecraft;

import com.mndk.bteterrarenderer.core.BTETerraRendererConstants;
import com.mndk.bteterrarenderer.core.gui.TextComponentManager;
import com.mndk.bteterrarenderer.core.util.mixin.MixinUtil;
import lombok.experimental.UtilityClass;

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
    public void sendErrorMessageToChat(String message) {
        sendTextComponentToChat(TextComponentManager.fromText("§c[" + BTETerraRendererConstants.NAME + "] " + message));
    }
    public void sendErrorMessageToChat(String message, Throwable t) {
        sendTextComponentToChat(TextComponentManager.fromText("§c[" + BTETerraRendererConstants.NAME + "] " + message));
        sendTextComponentToChat(TextComponentManager.fromText("§c[" + BTETerraRendererConstants.NAME + "] Reason: " + t.getMessage()));
        BTETerraRendererConstants.LOGGER.error(message, t);
    }

    public void playClickSound() {
        MixinUtil.notOverwritten();
    }
}
