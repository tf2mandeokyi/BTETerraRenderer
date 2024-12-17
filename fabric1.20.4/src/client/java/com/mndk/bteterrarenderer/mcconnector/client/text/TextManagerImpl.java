package com.mndk.bteterrarenderer.mcconnector.client.text;

import com.mndk.bteterrarenderer.mcconnector.client.gui.text.TextFormatCopy;
import com.mndk.bteterrarenderer.mcconnector.client.gui.text.TextManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import javax.annotation.Nonnull;

public class TextManagerImpl implements TextManager {
    public TextWrapper fromJson(@Nonnull String json) {
        Text text = Text.Serialization.fromJson(json);
        return text != null ? new TextWrapper(text) : null;
    }

    public TextWrapper fromString(@Nonnull String text) {
        return new TextWrapper(Text.literal(text));
    }

    public StyleWrapper emptyStyle() {
        return new StyleWrapper(Style.EMPTY);
    }

    public StyleWrapper styleWithColor(StyleWrapper styleWrapper, TextFormatCopy textColor) {
        Style style = styleWrapper.get();
        return new StyleWrapper(style.withColor(textColor.getColorIndex()));
    }

    public boolean handleClick(@Nonnull StyleWrapper styleWrapper) {
        Screen currentScreen = MinecraftClient.getInstance().currentScreen;
        if (currentScreen == null) return false;

        Style style = styleWrapper.get();
        return currentScreen.handleTextClick(style);
    }
}
