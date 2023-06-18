package com.mndk.bteterrarenderer.connector.gui;

import com.mndk.bteterrarenderer.connector.graphics.IScaledScreenSizeImpl12;
import com.mndk.bteterrarenderer.connector.minecraft.InputKey;
import com.mndk.bteterrarenderer.gui.components.AbstractGuiScreen;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Mouse;

import java.io.IOException;

public class AbstractGuiScreenImpl12 extends GuiScreen {
    public final AbstractGuiScreen delegate;
    private double pMouseX = 0, pMouseY = 0;

    public AbstractGuiScreenImpl12(AbstractGuiScreen delegate) {
        this.delegate = delegate;
        delegate.screenSize = IScaledScreenSizeImpl12::new;
    }

    public void initGui() {
        delegate.initGui();
    }
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        delegate.drawScreen(null, mouseX, mouseY, partialTicks);
    }
    public void updateScreen() {
        delegate.tick();
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

        int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
        delegate.mouseScrolled(mouseX, mouseY, Mouse.getEventDWheel());
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
        delegate.onClose();
    }
    public boolean doesGuiPauseGame() {
        return delegate.doesScreenPauseGame();
    }
}
