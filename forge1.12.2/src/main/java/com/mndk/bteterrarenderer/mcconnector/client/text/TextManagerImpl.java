package com.mndk.bteterrarenderer.mcconnector.client.text;

import com.mndk.bteterrarenderer.mcconnector.client.gui.text.TextFormatCopy;
import com.mndk.bteterrarenderer.mcconnector.client.gui.text.TextManager;
import com.mndk.bteterrarenderer.mcconnector.client.gui.screen.AbstractGuiScreenImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nonnull;

public class TextManagerImpl implements TextManager {
    public TextWrapper fromJson(@Nonnull String json) {
        ITextComponent textComponent = ITextComponent.Serializer.jsonToComponent(json);
        return textComponent != null ? new TextWrapperImpl(textComponent) : null;
    }

    public TextWrapper fromString(@Nonnull String text) {
        return new TextWrapperImpl(new TextComponentString(text));
    }

    public StyleWrapper emptyStyle() {
        return new StyleWrapperImpl(new Style());
    }

    public StyleWrapper styleWithColor(StyleWrapper styleWrapper, TextFormatCopy textColor) {
        Style style = ((StyleWrapperImpl) styleWrapper).delegate;
        TextFormatting formatting = TextFormatting.fromColorIndex(textColor.getColorIndex());
        if (formatting != null) style = style.createDeepCopy().setColor(formatting);
        return new StyleWrapperImpl(style);
    }

    public boolean handleClick(@Nonnull StyleWrapper styleWrapper) {
        GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;
        if (currentScreen == null) return false;
        if (!(currentScreen instanceof AbstractGuiScreenImpl)) return false;

        return ((AbstractGuiScreenImpl) currentScreen).handleStyleClick(((StyleWrapperImpl) styleWrapper).delegate);
    }
}
