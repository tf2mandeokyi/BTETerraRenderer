package com.mndk.bteterrarenderer.connector.gui;

import com.mndk.bteterrarenderer.connector.graphics.IScaledResolutionImpl;
import com.mndk.bteterrarenderer.connector.minecraft.InputKey;
import com.mndk.bteterrarenderer.gui.components.AbstractGuiScreen;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;

public class AbstractGuiScreenImpl extends GuiScreen {
    public final AbstractGuiScreen delegate;
    private double pMouseX = 0, pMouseY = 0;

    public AbstractGuiScreenImpl(AbstractGuiScreen delegate) {
        this.delegate = delegate;
        delegate.guiWidth = () -> super.width;
        delegate.minecraftDisplayWidth = () -> super.mc.displayWidth;
        delegate.scaledResolution = IScaledResolutionImpl::new;
    }

    public void initGui() {
        delegate.initGui();
    }
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        delegate.drawScreen(mouseX, mouseY, partialTicks);
    }
    public void updateScreen() {
        delegate.updateScreen();
    }
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        this.pMouseX = mouseX; this.pMouseY = mouseY;
        super.mouseClicked(mouseX, mouseY, mouseButton);
        delegate.mousePressed(mouseX, mouseY, mouseButton);
    }
    public void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        delegate.mouseReleased(mouseX, mouseY, state);
    }
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        delegate.handleMouseInput();
    }
    public void keyTyped(char key, int keyCode) throws IOException {
        super.keyTyped(key, keyCode);
        delegate.keyTyped(key, keyCode);
        delegate.keyPressed(InputKey.fromKeyboardCode(keyCode));
    }
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        delegate.mouseDragged(mouseX, mouseY, clickedMouseButton, pMouseX, pMouseY);
        this.pMouseX = mouseX; this.pMouseY = mouseY;
    }

    public void onGuiClosed() {
        delegate.onGuiClosed();
    }
    public boolean doesGuiPauseGame() {
        return delegate.doesGuiPauseGame();
    }
}
