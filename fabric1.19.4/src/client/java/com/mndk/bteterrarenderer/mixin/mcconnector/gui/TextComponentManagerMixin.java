package com.mndk.bteterrarenderer.mixin.mcconnector.gui;

import com.mndk.bteterrarenderer.mcconnector.gui.TextComponentManager;
import com.mndk.bteterrarenderer.mcconnector.wrapper.DrawContextWrapper;
import com.mndk.bteterrarenderer.mcconnector.wrapper.StyleWrapper;
import com.mndk.bteterrarenderer.mcconnector.wrapper.TextWrapper;
import com.mndk.bteterrarenderer.mod.mcconnector.gui.AbstractGuiScreenImpl;
import lombok.experimental.UtilityClass;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
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
            Text text = Text.Serializer.fromJson(json);
            return text != null ? new TextWrapper(text) : null;
        }

        public TextWrapper fromText(String text) {
            return new TextWrapper(Text.literal(text));
        }

        public boolean handleClick(@Nonnull StyleWrapper styleWrapper) {
            Screen currentScreen = MinecraftClient.getInstance().currentScreen;
            if(currentScreen == null) return false;

            Style style = styleWrapper.get();
            return currentScreen.handleTextClick(style);
        }

        public void handleStyleComponentHover(@Nonnull DrawContextWrapper drawContextWrapper, @Nonnull StyleWrapper styleWrapper, int x, int y) {
            Screen currentScreen = MinecraftClient.getInstance().currentScreen;
            if(!(currentScreen instanceof AbstractGuiScreenImpl guiScreen)) return;

            MatrixStack poseStack = drawContextWrapper.get();
            Style style = styleWrapper.get();
            guiScreen.renderTextHoverEffect(poseStack, style, x, y);
        }
    };}
}
