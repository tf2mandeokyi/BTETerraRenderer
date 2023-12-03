package com.mndk.bteterrarenderer.mcconnector.gui.component;

import com.mndk.bteterrarenderer.mcconnector.client.WindowManager;
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

    public final void drawScreen(Object poseStack, double mouseX, double mouseY, float partialTicks) {
        this.mouseHovered(mouseX, mouseY, partialTicks, false);
        this.drawScreen(poseStack);
    }
    protected abstract void drawScreen(Object poseStack);
    public abstract void tick();

    public abstract void onClose();
    public abstract boolean doesScreenPauseGame();
}
