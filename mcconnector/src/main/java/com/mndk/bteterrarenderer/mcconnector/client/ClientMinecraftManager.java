package com.mndk.bteterrarenderer.mcconnector.client;

import com.mndk.bteterrarenderer.mcconnector.CommonMinecraftManager;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.GlGraphicsManager;
import com.mndk.bteterrarenderer.mcconnector.client.gui.screen.AbstractGuiScreenCopy;
import com.mndk.bteterrarenderer.mcconnector.client.gui.screen.NativeGuiScreenWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.gui.text.TextManager;
import com.mndk.bteterrarenderer.mcconnector.client.input.GameInputManager;
import com.mndk.bteterrarenderer.mcconnector.client.text.FontWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.text.TextWrapper;
import com.mndk.bteterrarenderer.mcconnector.i18n.I18nManager;

import javax.annotation.Nullable;

public abstract class ClientMinecraftManager extends CommonMinecraftManager {

    public final GlGraphicsManager glGraphicsManager;
    public final GameInputManager inputManager;
    public final TextManager textManager;

    public ClientMinecraftManager(GameInputManager inputManager,
                                  GlGraphicsManager glGraphicsManager,
                                  I18nManager i18nManager,
                                  TextManager textManager) {
        super(i18nManager);
        this.inputManager = inputManager;
        this.glGraphicsManager = glGraphicsManager;
        this.textManager = textManager;
    }

    public abstract WindowDimension getWindowSize();
    public abstract FontWrapper<?> getDefaultFont();

    public abstract void displayGuiScreen(@Nullable AbstractGuiScreenCopy screen);
    public abstract NativeGuiScreenWrapper<?> newChatScreen(String initialText);

    public abstract boolean isOnMac();
    public abstract double getFovDegrees();
    public abstract double getPlayerRotationYaw();
    public abstract double getPlayerRotationPitch();
    public abstract void playClickSound();

    public abstract void sendTextComponentToChat(TextWrapper textComponent);
    public void sendFormattedStringToChat(String formattedString) {
        this.sendTextComponentToChat(this.textManager.fromString(formattedString));
    }
}
