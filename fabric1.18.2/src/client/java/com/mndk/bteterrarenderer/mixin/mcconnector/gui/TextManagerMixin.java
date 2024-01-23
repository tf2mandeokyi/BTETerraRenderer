package com.mndk.bteterrarenderer.mixin.mcconnector.gui;

import com.mndk.bteterrarenderer.mcconnector.gui.text.TextFormatCopy;
import com.mndk.bteterrarenderer.mcconnector.gui.text.TextManager;
import com.mndk.bteterrarenderer.mcconnector.wrapper.StyleWrapper;
import com.mndk.bteterrarenderer.mcconnector.wrapper.TextWrapper;
import lombok.experimental.UtilityClass;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import javax.annotation.Nonnull;

@UtilityClass
@Mixin(value = TextManager.class, remap = false)
public class TextManagerMixin {

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    private static TextManager makeDefault() { return new TextManager() {
        public TextWrapper fromJson(String json) {
            Text text = Text.Serializer.fromJson(json);
            return text != null ? new TextWrapper(text) : null;
        }

        public TextWrapper fromString(String text) {
            return new TextWrapper(new LiteralText(text));
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
            if(currentScreen == null) return false;

            Style style = styleWrapper.get();
            return currentScreen.handleTextClick(style);
        }
    };}
}
