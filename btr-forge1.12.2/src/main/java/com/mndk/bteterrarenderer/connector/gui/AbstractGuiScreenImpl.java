package com.mndk.bteterrarenderer.connector.gui;

import com.mndk.bteterrarenderer.connector.graphics.IScaledResolutionImpl;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;

public class AbstractGuiScreenImpl extends GuiScreen {
    public final AbstractGuiScreen delegate;

    public AbstractGuiScreenImpl(AbstractGuiScreen delegate) {
        this.delegate = delegate;
        delegate.guiWidth = () -> super.width;
        delegate.minecraftDisplayWidth = () -> super.mc.displayWidth;
        delegate.scaledResolution = IScaledResolutionImpl::new;
        delegate.fontRenderer = () -> new IFontRendererImpl(super.fontRenderer);
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
        super.mouseClicked(mouseX, mouseY, mouseButton);
        delegate.mouseClicked(mouseX, mouseY, mouseButton);
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
    }
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        delegate.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    public void onGuiClosed() {
        delegate.onGuiClosed();
    }
    public boolean doesGuiPauseGame() {
        return delegate.doesGuiPauseGame();
    }
}
