package com.mndk.bteterrarenderer.mixin.mcconnector.wrapper;

import com.mndk.bteterrarenderer.mcconnector.input.InputKey;
import com.mndk.bteterrarenderer.mcconnector.wrapper.DrawContextWrapper;
import com.mndk.bteterrarenderer.mcconnector.wrapper.NativeGuiScreenWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import javax.annotation.Nonnull;

@Mixin(value = NativeGuiScreenWrapper.class, remap = false)
public class MinecraftGuiScreenWrapperMixin {

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public static NativeGuiScreenWrapper<?> of(@Nonnull Object delegate) { return new NativeGuiScreenWrapper<Screen>(delegate) {
        public void onDisplayed() {}
        public void initGui(int width, int height) {
            getThisWrapped().init(Minecraft.getInstance(), width, height);
        }
        public void setScreenSize(int width, int height) {
            getThisWrapped().resize(Minecraft.getInstance(), width, height);
        }
        public void tick() {
            getThisWrapped().tick();
        }
        public void drawScreen(@Nonnull DrawContextWrapper<?> drawContextWrapper, int mouseX, int mouseY, float partialTicks) {
            getThisWrapped().render(drawContextWrapper.get(), mouseX, mouseY, partialTicks);
        }
        public boolean mousePressed(double mouseX, double mouseY, int mouseButton) {
            return getThisWrapped().mouseClicked(mouseX, mouseY, mouseButton);
        }
        public boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
            return getThisWrapped().mouseReleased(mouseX, mouseY, mouseButton);
        }
        public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double pMouseX, double pMouseY) {
            return getThisWrapped().mouseDragged(mouseX, mouseY, mouseButton, mouseX - pMouseX, mouseY - pMouseY);
        }
        public boolean mouseScrolled(double mouseX, double mouseY, double scrollAmount) {
            return getThisWrapped().mouseScrolled(mouseX, mouseY, scrollAmount);
        }
        public boolean charTyped(char typedChar, int keyCode) {
            return getThisWrapped().charTyped(typedChar, keyCode);
        }
        public boolean keyPressed(InputKey key, int scanCode, int modifiers) {
            return getThisWrapped().keyPressed(key.glfwKeyCode, scanCode, modifiers);
        }
        public void onRemoved() {
            getThisWrapped().removed();
        }
        public boolean doesScreenPauseGame() {
            return getThisWrapped().isPauseScreen();
        }
        public boolean shouldCloseOnEsc() {
            return getThisWrapped().shouldCloseOnEsc();
        }
        public boolean alsoListensForKeyPress() {
            return true;
        }
    };}

}
