package com.mndk.bteterrarenderer.mcconnector;

import com.mndk.bteterrarenderer.mcconnector.client.ClientMinecraftManager;
import com.mndk.bteterrarenderer.mcconnector.client.WindowDimension;
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
public class TestEnvironmentVirtualMinecraftManager extends ClientMinecraftManager {

    private static final TestEnvironmentVirtualMinecraftManager INSTANCE = new TestEnvironmentVirtualMinecraftManager(new File("test"));
    public static TestEnvironmentVirtualMinecraftManager getInstance() {
        return INSTANCE;
    }

    private final File gameDirectory;

    private TestEnvironmentVirtualMinecraftManager(File gameDirectory) {
        super(null, new DummyGlGraphicsManager(), new DummyI18nManager(), null);
        this.gameDirectory = gameDirectory;
    }

    @Override
    public File getGameDirectory() {
        return this.gameDirectory;
    }

    public ResourceLocationWrapper<?> newResourceLocation(String modId, String location) { throw unsupported(); }
    public WindowDimension getWindowSize() { throw unsupported(); }
    public FontWrapper<?> getDefaultFont() { throw unsupported(); }
    public void displayGuiScreen(@Nullable AbstractGuiScreenCopy screen) { throw unsupported(); }
    public NativeGuiScreenWrapper<?> newChatScreen(String initialText) { throw unsupported(); }
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
