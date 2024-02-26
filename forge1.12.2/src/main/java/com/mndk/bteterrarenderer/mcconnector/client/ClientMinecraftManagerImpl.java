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
import com.mndk.bteterrarenderer.mcconnector.config.MC12ForgeCfgConfigSaveLoader;
import com.mndk.bteterrarenderer.mcconnector.util.ResourceLocationWrapper;
import com.mndk.bteterrarenderer.mcconnector.util.ResourceLocationWrapperImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;

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
        return new MC12ForgeCfgConfigSaveLoader(configClass, modId);
    }

    @Override
    public WindowDimension getWindowSize() {
        return new WindowDimensionImpl();
    }

    @Override
    public FontWrapper<?> getDefaultFont() {
        return new FontWrapperImpl(Minecraft.getMinecraft().fontRenderer);
    }

    @Override
    public void displayGuiScreen(@Nullable AbstractGuiScreenCopy screen) {
        Minecraft.getMinecraft().displayGuiScreen(screen == null ? null : new AbstractGuiScreenImpl(screen));
    }

    @Override
    public NativeGuiScreenWrapper<?> newChatScreen(String initialText) {
        return new NativeGuiScreenWrapperImpl(new GuiChat(initialText));
    }

    public boolean isOnMac() {
        return Minecraft.IS_RUNNING_ON_MAC;
    }

    public double getPlayerRotationYaw() {
        return Minecraft.getMinecraft().player.rotationYaw;
    }

    public void sendTextComponentToChat(TextWrapper textComponent) {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        if(player == null) return;
        player.sendMessage(textComponent.get());
    }

    public void playClickSound() {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(
                SoundEvents.UI_BUTTON_CLICK, 1.0F
        ));
    }

    @Override
    public ResourceLocationWrapper<?> newResourceLocation(String modId, String location) {
        return new ResourceLocationWrapperImpl(new ResourceLocation(modId, location));
    }

    @Override
    public File getGameDirectory() {
        return Minecraft.getMinecraft().gameDir;
    }
}
