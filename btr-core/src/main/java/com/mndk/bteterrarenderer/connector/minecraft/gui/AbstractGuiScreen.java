package com.mndk.bteterrarenderer.connector.minecraft.gui;

import com.mndk.bteterrarenderer.connector.minecraft.graphics.IScaledResolution;

import java.io.IOException;

public abstract class AbstractGuiScreen {
    public abstract void initGui();
    public abstract void drawScreen(int mouseX, int mouseY, float partialTicks);
    public abstract void updateScreen();
    public abstract void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException;
    public abstract void mouseReleased(int mouseX, int mouseY, int state);
    public abstract void handleMouseInput() throws IOException;
    public abstract void keyTyped(char key, int keyCode) throws IOException;

    public abstract int getWidth();
    public abstract int minecraftDisplayWidth();
    public abstract IScaledResolution getScaledResolution();
    public abstract IFontRenderer getFontRenderer();
}
