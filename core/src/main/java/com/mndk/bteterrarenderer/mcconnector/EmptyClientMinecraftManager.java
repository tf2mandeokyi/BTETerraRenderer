package com.mndk.bteterrarenderer.mcconnector;

import com.mndk.bteterrarenderer.mcconnector.client.ClientMinecraftManager;
import com.mndk.bteterrarenderer.mcconnector.client.WindowDimension;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.BufferBuilderWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.gui.screen.AbstractGuiScreenCopy;
import com.mndk.bteterrarenderer.mcconnector.client.gui.screen.NativeGuiScreenWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.text.FontWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.text.TextWrapper;
import com.mndk.bteterrarenderer.mcconnector.util.ResourceLocationWrapper;

import javax.annotation.Nullable;
import java.io.File;

/**
 * Do not use this in game environments! This manager is only for tests.
 * */
public class EmptyClientMinecraftManager extends ClientMinecraftManager {

    private final File gameDirectory;

    public EmptyClientMinecraftManager(File gameDirectory) {
        super(null, null, null, null);
        this.gameDirectory = gameDirectory;
    }

    @Override
    public ResourceLocationWrapper<?> newResourceLocation(String modId, String location) {
        throw new UnsupportedOperationException("Minecraft is empty");
    }

    @Override
    public File getGameDirectory() {
        return this.gameDirectory;
    }

    @Override
    public WindowDimension getWindowSize() {
        throw new UnsupportedOperationException("Minecraft is empty");
    }

    @Override
    public FontWrapper<?> getDefaultFont() {
        throw new UnsupportedOperationException("Minecraft is empty");
    }

    @Override
    public BufferBuilderWrapper<?> tessellatorBufferBuilder() {
        throw new UnsupportedOperationException("Minecraft is empty");
    }

    @Override
    public void displayGuiScreen(@Nullable AbstractGuiScreenCopy screen) {
        throw new UnsupportedOperationException("Minecraft is empty");
    }

    @Override
    public NativeGuiScreenWrapper<?> newChatScreen(String initialText) {
        throw new UnsupportedOperationException("Minecraft is empty");
    }

    @Override
    public boolean isOnMac() {
        throw new UnsupportedOperationException("Minecraft is empty");
    }

    @Override
    public double getPlayerRotationYaw() {
        throw new UnsupportedOperationException("Minecraft is empty");
    }

    @Override
    public void playClickSound() {
        throw new UnsupportedOperationException("Minecraft is empty");
    }

    @Override
    public void sendTextComponentToChat(TextWrapper textComponent) {
        throw new UnsupportedOperationException("Minecraft is empty");
    }
}
