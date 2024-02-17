package com.mndk.bteterrarenderer.mod.mcconnector.gui;

import com.mndk.bteterrarenderer.mcconnector.input.InputKey;
import com.mndk.bteterrarenderer.mcconnector.gui.screen.AbstractGuiScreenCopy;
import com.mndk.bteterrarenderer.mcconnector.wrapper.DrawContextWrapper;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AbstractGuiScreenImpl extends Screen {
    public final AbstractGuiScreenCopy delegate;

    public AbstractGuiScreenImpl(@Nonnull AbstractGuiScreenCopy delegate) {
        super(TextComponent.EMPTY);
        this.delegate = delegate;
    }

    protected void init() {
        delegate.initGui(this.width, this.height);
    }
    public void resize(@Nonnull Minecraft client, int width, int height) {
        super.resize(client, width, height);
        delegate.setScreenSize(width, height);
    }
    public void tick() {
        delegate.tick();
    }
    public void render(@Nonnull PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        delegate.drawScreen(DrawContextWrapper.of(poseStack), mouseX, mouseY, partialTicks);
    }
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        return delegate.mousePressed(mouseX, mouseY, mouseButton);
    }
    public boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
        super.mouseReleased(mouseX, mouseY, mouseButton);
        return delegate.mouseReleased(mouseX, mouseY, mouseButton);
    }
    public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double dx, double dy) {
        super.mouseDragged(mouseX, mouseY, mouseButton, dx, dy);
        return delegate.mouseDragged(mouseX, mouseY, mouseButton, mouseX - dx, mouseY - dy);
    }
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollAmount) {
        super.mouseScrolled(mouseX, mouseY, scrollAmount);
        return delegate.mouseScrolled(mouseX, mouseY, scrollAmount);
    }
    public boolean keyPressed(int keyCode, int scanCode, int mods) {
        boolean superResult = super.keyPressed(keyCode, scanCode, mods);
        boolean delegateResult = delegate.keyPressed(InputKey.fromGlfwKeyCode(keyCode), scanCode, mods);
        return superResult || delegateResult;
    }
    public boolean charTyped(char typedChar, int keyCode) {
        super.charTyped(typedChar, keyCode);
        return delegate.charTyped(typedChar, keyCode);
    }

    public void removed() {
        delegate.onRemoved();
        super.removed();
    }
    public boolean isPauseScreen() {
        return delegate.doesScreenPauseGame();
    }
    public boolean shouldCloseOnEsc() {
        return delegate.shouldCloseOnEsc();
    }

    public void renderComponentHoverEffect(@Nonnull PoseStack poseStack, @Nullable Style style, int x, int y) {
        super.renderComponentHoverEffect(poseStack, style, x, y);
    }
}
