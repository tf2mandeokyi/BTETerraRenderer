package com.mndk.bteterrarenderer.mod.client.mixin.graphics;

import com.mndk.bteterrarenderer.core.gui.components.AbstractGuiScreenCopy;
import com.mndk.bteterrarenderer.core.input.InputKey;
import com.mndk.bteterrarenderer.mod.client.mixin.delegate.IScaledScreenSizeImpl;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;

public class AbstractGuiScreenImpl extends Screen {
    public final AbstractGuiScreenCopy delegate;

    public AbstractGuiScreenImpl(AbstractGuiScreenCopy delegate) {
        super(LiteralText.EMPTY);
        this.delegate = delegate;
        delegate.screenSize = IScaledScreenSizeImpl::new;
    }

    protected void init() {
        delegate.initGui();
    }
    public void tick() {
        delegate.tick();
    }
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        delegate.drawScreen(matrices, mouseX, mouseY, delta);
    }
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        return delegate.mousePressed(mouseX, mouseY, button);
    }
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        super.mouseReleased(mouseX, mouseY, button);
        return delegate.mouseReleased(mouseX, mouseY, button);
    }
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        return delegate.mouseDragged(mouseX, mouseY, button, mouseX - deltaX, mouseY - deltaY);
    }
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        super.mouseScrolled(mouseX, mouseY, amount);
        return delegate.mouseScrolled(mouseX, mouseY, amount);
    }
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean superResult = super.keyPressed(keyCode, scanCode, modifiers);
        boolean delegateResult = delegate.keyPressed(InputKey.fromGlfwKeyCode(keyCode));
        return superResult || delegateResult;
    }
    public boolean charTyped(char chr, int modifiers) {
        super.charTyped(chr, modifiers);
        return delegate.keyTyped(chr, modifiers);
    }

    public void close() {
        delegate.onClose();
        super.close();
    }
    public boolean shouldPause() {
        return delegate.doesScreenPauseGame();
    }
}
