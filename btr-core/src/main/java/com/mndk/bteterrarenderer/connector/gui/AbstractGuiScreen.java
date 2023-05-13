package com.mndk.bteterrarenderer.connector.gui;

import com.mndk.bteterrarenderer.connector.graphics.IScaledResolution;
import lombok.Setter;

import java.io.IOException;
import java.util.function.Supplier;

@Setter
public abstract class AbstractGuiScreen {
    protected Supplier<Integer> guiWidth, minecraftDisplayWidth;
    protected Supplier<IScaledResolution> scaledResolution;

    public abstract void initGui();
    public abstract void drawScreen(int mouseX, int mouseY, float partialTicks);
    public abstract void updateScreen();
    public abstract void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException;
    public abstract void mouseReleased(int mouseX, int mouseY, int state);
    public abstract void handleMouseInput() throws IOException;
    public abstract void keyTyped(char key, int keyCode) throws IOException;
    public abstract void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick);

    public abstract void onGuiClosed();
    public abstract boolean doesGuiPauseGame();
}
