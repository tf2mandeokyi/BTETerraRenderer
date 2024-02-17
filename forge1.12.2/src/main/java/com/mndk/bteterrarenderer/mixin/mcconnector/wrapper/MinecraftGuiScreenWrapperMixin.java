package com.mndk.bteterrarenderer.mixin.mcconnector.wrapper;

import com.mndk.bteterrarenderer.mcconnector.input.InputKey;
import com.mndk.bteterrarenderer.mcconnector.wrapper.DrawContextWrapper;
import com.mndk.bteterrarenderer.mcconnector.wrapper.NativeGuiScreenWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import javax.annotation.Nonnull;
import java.io.IOException;

@Mixin(value = NativeGuiScreenWrapper.class, remap = false)
public class MinecraftGuiScreenWrapperMixin {

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public static NativeGuiScreenWrapper<?> of(@Nonnull Object delegate) { return new NativeGuiScreenWrapper<GuiScreen>(delegate) {
        public void onDisplayed() {}
        public void initGui(int width, int height) {
            // This setWorldAndResolution calls initGui by itself, so there's no need to call it again
            getThisWrapped().setWorldAndResolution(Minecraft.getMinecraft(), width, height);
        }
        public void setScreenSize(int width, int height) {
            getThisWrapped().onResize(Minecraft.getMinecraft(), width, height);
        }
        public void tick() {
            getThisWrapped().updateScreen();
        }
        public void drawScreen(@Nonnull DrawContextWrapper<?> drawContextWrapper, int mouseX, int mouseY, float partialTicks) {
            getThisWrapped().drawScreen(mouseX, mouseY, partialTicks);
        }
        public boolean mousePressed(double mouseX, double mouseY, int mouseButton) {
            try { getThisWrapped().handleMouseInput(); } catch(IOException ignored) {}
            return false;
        }
        public boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
            try { getThisWrapped().handleMouseInput(); } catch(IOException ignored) {}
            return false;
        }
        public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double pMouseX, double pMouseY) {
            try { getThisWrapped().handleMouseInput(); } catch(IOException ignored) {}
            return false;
        }
        public boolean mouseScrolled(double mouseX, double mouseY, double scrollAmount) {
            try { getThisWrapped().handleMouseInput(); } catch(IOException ignored) {}
            return false;
        }
        public boolean charTyped(char typedChar, int keyCode) {
            try { getThisWrapped().handleKeyboardInput(); } catch(IOException ignored) {}
            return false;
        }
        public boolean keyPressed(InputKey key, int scanCode, int modifiers) {
            return false;
        }
        public void onRemoved() {
            getThisWrapped().onGuiClosed();
        }
        public boolean doesScreenPauseGame() {
            return getThisWrapped().doesGuiPauseGame();
        }
        public boolean shouldCloseOnEsc() {
            return true;
        }
        public boolean alsoListensForKeyPress() {
            return false;
        }
    };}

}
