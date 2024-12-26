package com.mndk.bteterrarenderer.mcconnector.client.text;

import com.mndk.bteterrarenderer.mcconnector.client.gui.text.TextFormatCopy;
import com.mndk.bteterrarenderer.mcconnector.client.gui.text.TextManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;

import javax.annotation.Nonnull;

public class TextManagerImpl implements TextManager {

    public TextWrapper fromJson(@Nonnull String json) {
        Component component = Component.Serializer.fromJson(json);
        return component != null ? new TextComponentWrapperImpl(component) : null;
    }

    public TextWrapper fromString(@Nonnull String text) {
        return new TextComponentWrapperImpl(new TextComponent(text));
    }

    public StyleWrapper emptyStyle() {
        return new StyleWrapperImpl(Style.EMPTY);
    }

    public StyleWrapper styleWithColor(StyleWrapper styleWrapper, TextFormatCopy textColor) {
        Style style = ((StyleWrapperImpl) styleWrapper).getWrapped();
        return new StyleWrapperImpl(style.withColor(textColor.getColorIndex()));
    }

    public boolean handleClick(@Nonnull StyleWrapper styleWrapper) {
        Screen currentScreen = Minecraft.getInstance().screen;
        if (currentScreen == null) return false;

        Style style = ((StyleWrapperImpl) styleWrapper).getWrapped();
        return currentScreen.handleComponentClicked(style);
    }
}
