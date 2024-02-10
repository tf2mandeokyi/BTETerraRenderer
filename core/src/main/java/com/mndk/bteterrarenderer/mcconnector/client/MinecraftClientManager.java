package com.mndk.bteterrarenderer.mcconnector.client;

import com.mndk.bteterrarenderer.mcconnector.MixinUtil;
import com.mndk.bteterrarenderer.mcconnector.gui.text.TextManager;
import com.mndk.bteterrarenderer.mcconnector.wrapper.TextWrapper;

/**
 * Contains miscellaneous utility methods related to the game
 */
public abstract class MinecraftClientManager {

    public static final MinecraftClientManager INSTANCE = makeInstance();
    private static MinecraftClientManager makeInstance() {
        return MixinUtil.notOverwritten();
    }

    public abstract boolean isOnMac();
    public abstract double getPlayerRotationYaw();
    public abstract void sendTextComponentToChat(TextWrapper textComponent);
    public abstract void playClickSound();

    public void sendFormattedStringToChat(String formattedString) {
        this.sendTextComponentToChat(TextManager.INSTANCE.fromString(formattedString));
    }
}
