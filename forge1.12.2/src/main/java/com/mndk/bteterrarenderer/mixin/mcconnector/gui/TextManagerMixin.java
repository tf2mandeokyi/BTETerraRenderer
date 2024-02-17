package com.mndk.bteterrarenderer.mixin.mcconnector.gui;

import com.mndk.bteterrarenderer.mcconnector.gui.text.TextFormatCopy;
import com.mndk.bteterrarenderer.mcconnector.gui.text.TextManager;
import com.mndk.bteterrarenderer.mcconnector.wrapper.StyleWrapper;
import com.mndk.bteterrarenderer.mcconnector.wrapper.TextWrapper;
import com.mndk.bteterrarenderer.mod.mcconnector.gui.AbstractGuiScreenImpl;
import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
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

        public TextWrapper fromJson(@Nonnull String json) {
            ITextComponent textComponent = ITextComponent.Serializer.jsonToComponent(json);
            return textComponent != null ? new TextWrapper(textComponent) : null;
        }

        public TextWrapper fromString(@Nonnull String text) {
            return new TextWrapper(new TextComponentString(text));
        }

        public StyleWrapper emptyStyle() {
            return new StyleWrapper(new Style());
        }

        public StyleWrapper styleWithColor(StyleWrapper styleWrapper, TextFormatCopy textColor) {
            Style style = styleWrapper.get();
            TextFormatting formatting = TextFormatting.fromColorIndex(textColor.getColorIndex());
            if(formatting != null) style = style.createDeepCopy().setColor(formatting);
            return new StyleWrapper(style);
        }

        public boolean handleClick(@Nonnull StyleWrapper styleWrapper) {
            GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;
            if(currentScreen == null) return false;
            if(!(currentScreen instanceof AbstractGuiScreenImpl)) return false;

            return ((AbstractGuiScreenImpl) currentScreen).handleStyleClick(styleWrapper.get());
        }
    };}
}
