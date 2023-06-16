package com.mndk.bteterrarenderer.gui.components;

import com.mndk.bteterrarenderer.connector.graphics.IScaledScreenSize;
import lombok.Setter;

import java.util.function.Supplier;

@Setter
public abstract class AbstractGuiScreen implements GuiEventListenerImpl {
    public Supplier<Integer> guiWidth;
    public Supplier<IScaledScreenSize> screenSize;

    public abstract void initGui();
    public abstract void drawScreen(Object poseStack, double mouseX, double mouseY, float partialTicks);
    public abstract void tick();

    public abstract void onClose();
    public abstract boolean doesScreenPauseGame();
}