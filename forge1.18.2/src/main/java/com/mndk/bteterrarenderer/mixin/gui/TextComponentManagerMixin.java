package com.mndk.bteterrarenderer.mixin.gui;

import com.mndk.bteterrarenderer.core.gui.TextComponentManager;
import com.mndk.bteterrarenderer.mod.client.gui.AbstractGuiScreenImpl;
import com.mojang.blaze3d.vertex.PoseStack;
import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@UtilityClass
@Mixin(value = TextComponentManager.class, remap = false)
public class TextComponentManagerMixin {

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public Object fromJson(String json) {
        return Component.Serializer.fromJson(json);
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Nullable
    @Overwrite
    public Object getStyleComponentFromLine(@Nonnull Object lineComponent, int mouseXFromLeft) {
        if(lineComponent instanceof FormattedText text) {
            return Minecraft.getInstance().font.getSplitter().componentStyleAtWidth(text, mouseXFromLeft);
        }
        else if(lineComponent instanceof FormattedCharSequence sequence) {
            return Minecraft.getInstance().font.getSplitter().componentStyleAtWidth(sequence, mouseXFromLeft);
        }
        return null;
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public boolean handleClick(@Nonnull Object styleComponent) {
        Screen currentScreen = Minecraft.getInstance().screen;
        if(currentScreen == null) return false;
        return currentScreen.handleComponentClicked((Style) styleComponent);
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public void handleStyleComponentHover(@Nonnull Object poseStack, @Nonnull Object styleComponent, int x, int y) {
        Screen currentScreen = Minecraft.getInstance().screen;
        if(!(currentScreen instanceof AbstractGuiScreenImpl guiScreen)) return;
        guiScreen.renderComponentHoverEffect((PoseStack) poseStack, (Style) styleComponent, x, y);
    }

}
