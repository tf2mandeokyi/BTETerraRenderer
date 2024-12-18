package com.mndk.bteterrarenderer.mcconnector.client;

import com.mndk.bteterrarenderer.mcconnector.client.graphics.GlGraphicsManagerImpl;
import com.mndk.bteterrarenderer.mcconnector.client.gui.screen.AbstractGuiScreenCopy;
import com.mndk.bteterrarenderer.mcconnector.client.gui.screen.AbstractGuiScreenImpl;
import com.mndk.bteterrarenderer.mcconnector.client.gui.screen.NativeGuiScreenWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.gui.screen.NativeGuiScreenWrapperImpl;
import com.mndk.bteterrarenderer.mcconnector.client.i18n.ClientI18nManagerImpl;
import com.mndk.bteterrarenderer.mcconnector.client.input.GameInputManagerImpl;
import com.mndk.bteterrarenderer.mcconnector.client.text.FontWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.text.FontWrapperImpl;
import com.mndk.bteterrarenderer.mcconnector.client.text.TextManagerImpl;
import com.mndk.bteterrarenderer.mcconnector.client.text.TextWrapper;
import com.mndk.bteterrarenderer.mcconnector.config.AbstractConfigSaveLoader;
import com.mndk.bteterrarenderer.mcconnector.config.ForgeTomlConfigSaveLoader;
import com.mndk.bteterrarenderer.mcconnector.util.ResourceLocationWrapper;
import com.mndk.bteterrarenderer.mcconnector.util.ResourceLocationWrapperImpl;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;

import javax.annotation.Nullable;
import java.io.File;

public class ClientMinecraftManagerImpl extends ClientMinecraftManager {

    public ClientMinecraftManagerImpl() {
        super(
                new GameInputManagerImpl(),
                new GlGraphicsManagerImpl(),
                new ClientI18nManagerImpl(),
                new TextManagerImpl()
        );
    }

    @Override
    public AbstractConfigSaveLoader newConfigSaveLoader(Class<?> configClass, String modId) {
        return new ForgeTomlConfigSaveLoader(configClass);
    }

    @Override
    public WindowDimension getWindowSize() {
        return new WindowDimensionImpl(Minecraft.getInstance().getWindow());
    }

    @Override
    public FontWrapper<?> getDefaultFont() {
        return new FontWrapperImpl(Minecraft.getInstance().font);
    }

    @Override
    public void displayGuiScreen(@Nullable AbstractGuiScreenCopy screen) {
        Minecraft.getInstance().setScreen(screen == null ? null : new AbstractGuiScreenImpl(screen));
    }

    @Override
    public NativeGuiScreenWrapper<?> newChatScreen(String initialText) {
        return new NativeGuiScreenWrapperImpl(new ChatScreen(initialText));
    }

    public boolean isOnMac() {
        return Minecraft.ON_OSX;
    }

    public double getFovDegrees() {
        return Minecraft.getInstance().options.fov;
    }

    public double getPlayerRotationYaw() {
        LocalPlayer player = Minecraft.getInstance().player;
        return player != null ? player.getYRot() : 0;
    }

    public double getPlayerRotationPitch() {
        LocalPlayer player = Minecraft.getInstance().player;
        return player != null ? player.getXRot() : 0;
    }

    public void sendTextComponentToChat(TextWrapper textComponent) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        player.sendMessage(textComponent.get(), Util.NIL_UUID);
    }

    public void playClickSound() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(
                SoundEvents.UI_BUTTON_CLICK, 1.0f
        ));
    }

    @Override
    public ResourceLocationWrapper<?> newResourceLocation(String modId, String location) {
        return new ResourceLocationWrapperImpl(new ResourceLocation(modId, location));
    }

    @Override
    public File getGameDirectory() {
        // Note: Server version of this is FMLLoader.getGamePath().toFile()
        return Minecraft.getInstance().gameDirectory;
    }
}
