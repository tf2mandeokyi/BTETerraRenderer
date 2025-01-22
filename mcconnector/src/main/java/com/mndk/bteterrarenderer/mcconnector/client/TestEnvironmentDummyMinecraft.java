package com.mndk.bteterrarenderer.mcconnector.client;

import com.mndk.bteterrarenderer.mcconnector.client.graphics.DummyGlGraphicsManager;
import com.mndk.bteterrarenderer.mcconnector.client.gui.screen.AbstractGuiScreenCopy;
import com.mndk.bteterrarenderer.mcconnector.client.gui.screen.NativeGuiScreenWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.text.FontWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.text.TextWrapper;
import com.mndk.bteterrarenderer.mcconnector.i18n.DummyI18nManager;
import com.mndk.bteterrarenderer.mcconnector.util.ResourceLocationWrapper;

import javax.annotation.Nullable;
import java.io.File;

/**
 * Do not use this in game environments! This manager is only for tests.
 * */
public class TestEnvironmentDummyMinecraft extends ClientMinecraftManager {

    private static final TestEnvironmentDummyMinecraft INSTANCE = new TestEnvironmentDummyMinecraft(new File("test"));
    public static TestEnvironmentDummyMinecraft getInstance() {
        return INSTANCE;
    }

    private final File gameDirectory;

    private TestEnvironmentDummyMinecraft(File gameDirectory) {
        super(null, new DummyGlGraphicsManager(), new DummyI18nManager(), null, null);
        this.gameDirectory = gameDirectory;
    }

    @Override
    public File getGameDirectory() {
        return this.gameDirectory;
    }

    public ResourceLocationWrapper newResourceLocation(String modId, String location) { throw unsupported(); }
    public WindowDimension getWindowSize() { throw unsupported(); }
    public FontWrapper getDefaultFont() { throw unsupported(); }
    public void displayGuiScreen(@Nullable AbstractGuiScreenCopy screen) { throw unsupported(); }
    public NativeGuiScreenWrapper newChatScreen(String initialText) { throw unsupported(); }
    public boolean isOnMac() { throw unsupported(); }
    public double getFovDegrees() { throw unsupported(); }
    public double getPlayerRotationYaw() { throw unsupported(); }
    public double getPlayerRotationPitch() { throw unsupported(); }
    public void playClickSound() { throw unsupported(); }
    public void sendTextComponentToChat(TextWrapper textComponent) { throw unsupported(); }

    public static UnsupportedOperationException unsupported() {
        return new UnsupportedOperationException("Minecraft is empty");
    }
}
