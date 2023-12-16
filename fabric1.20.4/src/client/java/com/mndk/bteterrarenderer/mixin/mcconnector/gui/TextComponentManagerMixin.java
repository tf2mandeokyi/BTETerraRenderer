package com.mndk.bteterrarenderer.mixin.mcconnector.gui;

import com.mndk.bteterrarenderer.mcconnector.gui.TextComponentManager;
import lombok.experimental.UtilityClass;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
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
    public Object fromJson(String json) {
        return Text.Serialization.fromJson(json);
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public Object fromText(String text) {
        return Text.literal(text);
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public boolean handleClick(@Nonnull Object styleComponent) {
        Screen currentScreen = MinecraftClient.getInstance().currentScreen;
        if(currentScreen == null) return false;
        return currentScreen.handleTextClick((Style) styleComponent);
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public void handleStyleComponentHover(@Nonnull Object drawContext, @Nonnull Object styleComponent, int x, int y) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        ((DrawContext) drawContext).drawHoverEvent(textRenderer, (Style) styleComponent, x, y);
    }

}
