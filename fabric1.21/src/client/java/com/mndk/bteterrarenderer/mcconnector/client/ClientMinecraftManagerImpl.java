package com.mndk.bteterrarenderer.mcconnector.client;

import com.mndk.bteterrarenderer.mcconnector.client.graphics.BufferBuildersManagerImpl;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.GlGraphicsManagerImpl;
import com.mndk.bteterrarenderer.mcconnector.client.gui.screen.AbstractGuiScreenCopy;
import com.mndk.bteterrarenderer.mcconnector.client.gui.screen.AbstractGuiScreenImpl;
import com.mndk.bteterrarenderer.mcconnector.client.gui.screen.NativeGuiScreenWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.gui.screen.NativeGuiScreenWrapperImpl;
import com.mndk.bteterrarenderer.mcconnector.client.i18n.ClientI18nManagerImpl;
import com.mndk.bteterrarenderer.mcconnector.client.input.GameInputManagerImpl;
import com.mndk.bteterrarenderer.mcconnector.client.text.*;
import com.mndk.bteterrarenderer.mcconnector.util.ResourceLocationWrapper;
import com.mndk.bteterrarenderer.mcconnector.util.ResourceLocationWrapperImpl;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

import javax.annotation.Nullable;
import java.io.File;

public class ClientMinecraftManagerImpl extends ClientMinecraftManager {

    public ClientMinecraftManagerImpl() {
        super(
                new GameInputManagerImpl(), new GlGraphicsManagerImpl(), new ClientI18nManagerImpl(),
                new TextManagerImpl(), new BufferBuildersManagerImpl()
        );
    }

    @Override
    public WindowDimension getWindowSize() {
        return new WindowDimensionImpl(MinecraftClient.getInstance().getWindow());
    }

    @Override
    public FontWrapper getDefaultFont() {
        return new FontWrapperImpl(MinecraftClient.getInstance().textRenderer);
    }

    @Override
    public ResourceLocationWrapper newResourceLocation(String modId, String location) {
        return new ResourceLocationWrapperImpl(Identifier.of(modId, location));
    }

    @Override
    public File getGameDirectory() {
        return FabricLoader.getInstance().getGameDir().toFile();
    }

    @Override
    public File getConfigDirectory() {
        return FabricLoader.getInstance().getConfigDir().toFile();
    }

    @Override
    public void displayGuiScreen(@Nullable AbstractGuiScreenCopy screen) {
        MinecraftClient.getInstance().setScreen(screen == null ? null : new AbstractGuiScreenImpl(screen));
    }

    @Override
    public NativeGuiScreenWrapper newChatScreen(String initialText) {
        return new NativeGuiScreenWrapperImpl(new ChatScreen(initialText));
    }

    public boolean isOnMac() {
        return MinecraftClient.IS_SYSTEM_MAC;
    }

    public double getFovDegrees() {
        return MinecraftClient.getInstance().options.getFov().getValue();
    }

    public double getPlayerRotationYaw() {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        return player != null ? player.getYaw() : 0;
    }

    public double getPlayerRotationPitch() {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        return player != null ? player.getPitch() : 0;
    }

    public void sendTextComponentToChat(TextWrapper textComponent) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return;
        player.sendMessage(((TextWrapperImpl) textComponent).delegate, false);
    }

    public void playClickSound() {
        MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(
                SoundEvents.UI_BUTTON_CLICK, 1.0f
        ));
    }
}
