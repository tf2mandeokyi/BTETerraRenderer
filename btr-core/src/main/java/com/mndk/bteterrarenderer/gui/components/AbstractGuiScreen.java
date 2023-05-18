package com.mndk.bteterrarenderer.gui.components;

import com.mndk.bteterrarenderer.connector.graphics.IScaledResolution;
import lombok.Setter;

import java.io.IOException;
import java.util.function.Supplier;

@Setter
public abstract class AbstractGuiScreen implements GuiEventListenerImpl {
    public Supplier<Integer> guiWidth, minecraftDisplayWidth;
    public Supplier<IScaledResolution> scaledResolution;

    public abstract void initGui();
    public abstract void drawScreen(double mouseX, double mouseY, float partialTicks);
    public abstract void updateScreen();
    public abstract boolean mousePressed(double mouseX, double mouseY, int mouseButton);
    public abstract void mouseReleased(double mouseX, double mouseY, int mouseButton);
    public abstract void handleMouseInput() throws IOException;
    public abstract boolean keyTyped(char key, int keyCode);
    public abstract void mouseDragged(double mouseX, double mouseY, int clickedMouseButton, double pMouseX, double pMouseY);

    public abstract void onGuiClosed();
    public abstract boolean doesGuiPauseGame();
}
