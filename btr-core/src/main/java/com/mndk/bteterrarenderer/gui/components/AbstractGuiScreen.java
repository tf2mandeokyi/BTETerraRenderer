package com.mndk.bteterrarenderer.gui.components;

import com.mndk.bteterrarenderer.connector.graphics.IScaledScreenSize;
import lombok.Setter;

import java.util.function.Supplier;

@Setter
public abstract class AbstractGuiScreen implements GuiEventListenerCopy {
    public Supplier<IScaledScreenSize> screenSize;

    public abstract void initGui();
    public final void drawScreen(Object poseStack, double mouseX, double mouseY, float partialTicks) {
        this.mouseHovered(mouseX, mouseY, partialTicks, false);
        this.drawScreen(poseStack);
    }
    protected abstract void drawScreen(Object poseStack);
    public abstract void tick();

    public abstract void onClose();
    public abstract boolean doesScreenPauseGame();
}
