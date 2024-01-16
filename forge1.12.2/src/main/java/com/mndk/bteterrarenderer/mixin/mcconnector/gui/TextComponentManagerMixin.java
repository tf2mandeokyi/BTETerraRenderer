package com.mndk.bteterrarenderer.mixin.mcconnector.gui;

import com.mndk.bteterrarenderer.mcconnector.gui.TextComponentManager;
import com.mndk.bteterrarenderer.mcconnector.wrapper.DrawContextWrapper;
import com.mndk.bteterrarenderer.mcconnector.wrapper.StyleWrapper;
import com.mndk.bteterrarenderer.mcconnector.wrapper.TextWrapper;
import com.mndk.bteterrarenderer.mod.mcconnector.gui.AbstractGuiScreenImpl;
import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
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
            ITextComponent textComponent = ITextComponent.Serializer.jsonToComponent(json);
            return textComponent != null ? new TextWrapper(textComponent) : null;
        }

        public TextWrapper fromText(String text) {
            return new TextWrapper(new TextComponentString(text));
        }

        public boolean handleClick(@Nonnull StyleWrapper styleWrapper) {
            GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;
            if(currentScreen == null) return false;
            return currentScreen.handleComponentClick(styleWrapper.get());
        }

        public void handleStyleComponentHover(DrawContextWrapper drawContextWrapper, StyleWrapper styleWrapper, int x, int y) {
            GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;
            if(currentScreen == null) return;
            if(!(currentScreen instanceof AbstractGuiScreenImpl)) return;

            ((AbstractGuiScreenImpl) currentScreen).handleComponentHover(styleWrapper.get(), x, y);
        }
    };}
}
