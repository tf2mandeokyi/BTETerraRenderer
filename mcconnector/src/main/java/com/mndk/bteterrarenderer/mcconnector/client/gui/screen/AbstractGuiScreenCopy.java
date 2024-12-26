package com.mndk.bteterrarenderer.mcconnector.client.gui.screen;

import com.mndk.bteterrarenderer.mcconnector.client.graphics.DrawContextWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.gui.component.GuiComponentCopy;
import lombok.Getter;

import javax.annotation.Nonnull;

@Getter
public abstract class AbstractGuiScreenCopy implements GuiScreenCopy {

    /** Screen dimensions */
    private int width, height;

    @Override
    public final void initGui(int width, int height) {
        this.setScreenSize(width, height);
        this.initGui();
    }
    protected abstract void initGui();

    @Override
    public void setScreenSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public final void drawScreen(@Nonnull DrawContextWrapper drawContextWrapper, int mouseX, int mouseY, float partialTicks) {
        this.mouseHovered(mouseX, mouseY, partialTicks);
        this.drawScreen(drawContextWrapper);
    }
    public abstract void drawScreen(@Nonnull DrawContextWrapper drawContextWrapper);

    /**
     * This should be called before the {@link GuiComponentCopy#drawComponent} call.
     * @return {@code true} if the component has reacted the mouse hover. {@code false} otherwise.
     */
    public abstract boolean mouseHovered(int mouseX, int mouseY, float partialTicks);
    public abstract boolean isChatFocused();
}
