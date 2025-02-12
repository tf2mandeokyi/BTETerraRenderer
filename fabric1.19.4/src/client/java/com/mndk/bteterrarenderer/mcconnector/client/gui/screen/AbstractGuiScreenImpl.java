package com.mndk.bteterrarenderer.mcconnector.client.gui.screen;

import com.mndk.bteterrarenderer.mcconnector.client.gui.GuiDrawContextWrapperImpl;
import com.mndk.bteterrarenderer.mcconnector.client.input.InputKey;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        delegate.drawScreen(new GuiDrawContextWrapperImpl(matrices), mouseX, mouseY, delta);
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

    public void renderTextHoverEffect(MatrixStack matrices, @Nullable Style style, int x, int y) {
        super.renderTextHoverEffect(matrices, style, x, y);
    }
}
