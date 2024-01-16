package com.mndk.bteterrarenderer.mcconnector.gui.component;

import com.mndk.bteterrarenderer.mcconnector.client.WindowManager;
import com.mndk.bteterrarenderer.mcconnector.wrapper.DrawContextWrapper;
import lombok.Setter;

@Setter
public abstract class AbstractGuiScreenCopy implements GuiEventListenerCopy {
    public abstract void initGui();

    protected int getWidth() {
        return WindowManager.getScaledWidth();
    }
    protected int getHeight() {
        return WindowManager.getScaledHeight();
    }

    public final void drawScreen(DrawContextWrapper drawContextWrapper, double mouseX, double mouseY, float partialTicks) {
        this.mouseHovered(mouseX, mouseY, partialTicks, false);
        this.drawScreen(drawContextWrapper);
    }
    protected abstract void drawScreen(DrawContextWrapper drawContextWrapper);
    public abstract void tick();

    public abstract void onClose();
    public abstract boolean doesScreenPauseGame();
}
