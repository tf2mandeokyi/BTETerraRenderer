package com.mndk.bteterrarenderer.mixin.gui;

import com.mndk.bteterrarenderer.core.gui.FontManager;
import com.mndk.bteterrarenderer.core.gui.TextComponentManager;
import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.event.HoverEvent;
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
        return ITextComponent.Serializer.jsonToComponent(json);
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Nullable
    @Overwrite
    public Object getStyleComponentFromLine(@Nonnull Object lineComponent, int mouseXFromLeft) {
        int xPos = 0;
        ITextComponent textComponent = (ITextComponent) lineComponent, clicked = null;
        for(ITextComponent child : textComponent.getSiblings()) {
            int childWidth = FontManager.getComponentWidth(child);
            if(xPos <= mouseXFromLeft && mouseXFromLeft <= xPos + childWidth) {
                clicked = child;
                break;
            }
            xPos += childWidth;
        }
        return clicked;
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public boolean handleClick(@Nonnull Object styleComponent) {
        GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;
        if(currentScreen == null) return false;
        return currentScreen.handleComponentClick((ITextComponent) styleComponent);
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Nullable
    @Overwrite
    public Object getTooltipTextFromStyleComponent(@Nonnull Object styleComponent) {
        HoverEvent hoverEvent = ((ITextComponent) styleComponent).getStyle().getHoverEvent();
        return hoverEvent == null ? null : hoverEvent.getValue();
    }

}
