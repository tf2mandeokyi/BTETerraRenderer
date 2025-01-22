package com.mndk.bteterrarenderer.mcconnector.client.gui.screen;

import com.mndk.bteterrarenderer.mcconnector.client.graphics.GuiDrawContextWrapperImpl;
import com.mndk.bteterrarenderer.mcconnector.client.input.InputKey;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import javax.annotation.Nonnull;

public class AbstractGuiScreenImpl extends Screen {
    public final AbstractGuiScreenCopy delegate;

    public AbstractGuiScreenImpl(@Nonnull AbstractGuiScreenCopy delegate) {
        super(Text.empty());
        this.delegate = delegate;
    }

    protected void init() {
        delegate.initGui(this.width, this.height);
    }
    public void resize(MinecraftClient client, int width, int height) {
        super.resize(client, width, height);
        delegate.setScreenSize(width, height);
    }
    public void tick() {
        delegate.tick();
    }
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        delegate.drawScreen(new GuiDrawContextWrapperImpl(context), mouseX, mouseY, delta);
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
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        return delegate.mouseScrolled(mouseX, mouseY, verticalAmount);
    }
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean superResult = super.keyPressed(keyCode, scanCode, modifiers);
        boolean delegateResult = delegate.keyPressed(InputKey.fromGlfwKeyCode(keyCode), scanCode, modifiers);
        return superResult || delegateResult;
    }
    public boolean charTyped(char chr, int modifiers) {
        super.charTyped(chr, modifiers);
        return delegate.charTyped(chr, modifiers);
    }

    public void removed() {
        delegate.onRemoved();
        super.removed();
    }
    public boolean shouldPause() {
        return delegate.doesScreenPauseGame();
    }
    public boolean shouldCloseOnEsc() {
        return delegate.shouldCloseOnEsc();
    }
}
