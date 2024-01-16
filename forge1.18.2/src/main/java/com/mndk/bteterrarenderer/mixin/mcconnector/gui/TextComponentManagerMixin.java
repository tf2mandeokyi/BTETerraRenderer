package com.mndk.bteterrarenderer.mixin.mcconnector.gui;

import com.mndk.bteterrarenderer.mcconnector.gui.TextComponentManager;
import com.mndk.bteterrarenderer.mcconnector.wrapper.DrawContextWrapper;
import com.mndk.bteterrarenderer.mcconnector.wrapper.StyleWrapper;
import com.mndk.bteterrarenderer.mcconnector.wrapper.TextWrapper;
import com.mndk.bteterrarenderer.mod.mcconnector.gui.AbstractGuiScreenImpl;
import com.mojang.blaze3d.vertex.PoseStack;
import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import javax.annotation.Nonnull;

@UtilityClass
@Mixin(value = TextComponentManager.class, remap = false)
public class TextComponentManagerMixin {

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    private static TextComponentManager makeDefault() { return new TextComponentManager() {
        public TextWrapper fromJson(String json) {
            Component component = Component.Serializer.fromJson(json);
            return component != null ? new TextWrapper(component) : null;
        }

        public TextWrapper fromText(String text) {
            return new TextWrapper(new TextComponent(text));
        }

        public boolean handleClick(@Nonnull StyleWrapper styleWrapper) {
            Screen currentScreen = Minecraft.getInstance().screen;
            if(currentScreen == null) return false;

            Style style = styleWrapper.get();
            return currentScreen.handleComponentClicked(style);
        }

        public void handleStyleComponentHover(@Nonnull DrawContextWrapper drawContextWrapper, @Nonnull StyleWrapper styleWrapper, int x, int y) {
            Screen currentScreen = Minecraft.getInstance().screen;
            if(!(currentScreen instanceof AbstractGuiScreenImpl guiScreen)) return;

            PoseStack poseStack = drawContextWrapper.get();
            Style style = styleWrapper.get();
            guiScreen.renderComponentHoverEffect(poseStack, style, x, y);
        }
    };}
}
