package com.mndk.bteterrarenderer.connector.gui;

import com.mndk.bteterrarenderer.connector.graphics.IScaledScreenSizeImpl;
import com.mndk.bteterrarenderer.connector.minecraft.InputKey;
import com.mndk.bteterrarenderer.gui.components.AbstractGuiScreen;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;

public class AbstractGuiScreenImpl extends Screen {
    public final AbstractGuiScreen delegate;

    protected AbstractGuiScreenImpl(AbstractGuiScreen delegate) {
        super(TextComponent.EMPTY);
        this.delegate = delegate;
        delegate.guiWidth = () -> super.width;
        delegate.screenSize = IScaledScreenSizeImpl::new;
    }

    @Override
    protected void init() {
        delegate.initGui();
    }

    @Override
    public void tick() {
        delegate.updateScreen();
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        delegate.drawScreen(poseStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        return delegate.mousePressed(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
        super.mouseReleased(mouseX, mouseY, mouseButton);
        return delegate.mouseReleased(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double dx, double dy) {
        super.mouseDragged(mouseX, mouseY, mouseButton, dx, dy);
        return delegate.mouseDragged(mouseX, mouseY, mouseButton, mouseX - dx, mouseY - dy);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollAmount) {
        super.mouseScrolled(mouseX, mouseY, scrollAmount);
        return delegate.mouseScrolled(mouseX, mouseY, scrollAmount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int mods) {
        super.keyPressed(keyCode, scanCode, mods);
        return delegate.keyPressed(InputKey.fromGlfwKeyCode(keyCode));
    }

    @Override
    public boolean charTyped(char typedChar, int keyCode) {
        super.charTyped(typedChar, keyCode);
        return delegate.keyTyped(typedChar, keyCode);
    }

    @Override
    public void onClose() {
        delegate.onClose();
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return delegate.doesScreenPauseGame();
    }
}
