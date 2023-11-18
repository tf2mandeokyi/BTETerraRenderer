package com.mndk.bteterrarenderer.core.gui.sidebar;

import com.mndk.bteterrarenderer.core.gui.RawGuiManager;
import com.mndk.bteterrarenderer.core.util.mixin.MixinDelegateCreator;
import com.mndk.bteterrarenderer.core.gui.components.IGuiChat;
import lombok.Setter;

import javax.annotation.Nullable;

/**
 * TODO: Make chat history also be shown
 */
public class SidebarGuiChat {

    @Setter
    private boolean opened;
    @Nullable
    private final IGuiChat parent;

    private int left, right;

    public SidebarGuiChat() {
        this.parent = MixinDelegateCreator.newGuiChat();
        this.opened = false;
    }

    public boolean isAvailable() {
        return parent != null;
    }

    public boolean isOpened() {
        return opened && isAvailable();
    }

    public void initGui() {
        this.left = 0;

        if(parent == null) return;
        parent.init();
    }

    public void setText(String newChatText, boolean shouldOverwrite) {
        if(parent == null) return;
        parent.setText(newChatText, shouldOverwrite);
    }

    public void changeSideMargin(SidebarSide side, int sidebarWidth) {
        if(parent == null) return;
        this.changeSideMargin(
                side == SidebarSide.LEFT ? sidebarWidth : 0,
                side == SidebarSide.LEFT ? parent.getWidth() : parent.getWidth() - sidebarWidth
        );
    }

    public void changeSideMargin(int left, int right) {
        this.left = left;
        this.right = right;

        if(parent == null) return;
        parent.setInputFieldX(left + 4);
        parent.setInputFieldWidth(right - left - 4);
    }

    public void mouseHovered(double mouseX, double mouseY, float partialTicks) {
        if(parent == null) return;
        parent.handleMouseHover(mouseX, mouseY, partialTicks);
    }

    public void drawScreen(Object poseStack) {
        if(parent == null) return;

        RawGuiManager.fillRect(poseStack,
                left + 2, parent.getHeight() - 14,
                right - 2, parent.getHeight() - 2, Integer.MIN_VALUE
        );
        parent.drawInputFieldBox();
    }

    public boolean keyTypedResponse(char typedChar, int keyCode) {
        if(parent == null) return false;
        if(!parent.isInputFieldFocused()) return false;

        if (keyCode == 28 || keyCode == 156) {
            String s = parent.getInputFieldText().trim();
            if (!s.isEmpty()) parent.sendChatMessage(s);
            this.opened = false;
            return true;
        }
        parent.keyTyped(typedChar, keyCode);
        return true;
    }

    public boolean mouseClickResponse(double mouseX, double mouseY, int mouseButton) {
        if(parent == null) return false;

        if (mouseButton == 0) {
            boolean componentClicked = parent.handleMouseClick(mouseX, mouseY, mouseButton);
            if(componentClicked) return true;
            parent.setInputFieldFocused(
                    mouseX >= this.left && mouseX <= this.right &&
                            mouseY >= parent.getHeight() - 16 && mouseY <= parent.getHeight()
            );
        }

        return parent.inputFieldMouseClicked(mouseX, mouseY, mouseButton);
    }

    public void updateScreen() {
        if(parent == null) return;
        parent.updateScreen();
    }

    public void handleMouseInput() {
        if(parent == null) return;
        parent.handleMouseInput();
    }
}
