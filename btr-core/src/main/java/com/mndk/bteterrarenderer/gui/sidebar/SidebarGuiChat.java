package com.mndk.bteterrarenderer.gui.sidebar;

import com.mndk.bteterrarenderer.connector.DependencyConnectorSupplier;
import com.mndk.bteterrarenderer.connector.graphics.IScaledResolution;
import com.mndk.bteterrarenderer.connector.gui.GuiStaticConnector;
import com.mndk.bteterrarenderer.connector.gui.IGuiChat;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;

public class SidebarGuiChat {

    @Getter @Setter
    private boolean opened;
    private final IGuiChat parent;

    private int left, right;

    public SidebarGuiChat() {
        this.parent = DependencyConnectorSupplier.INSTANCE.newGuiChat();
        this.opened = false;
    }


    public void initGui(IScaledResolution resolution) {
        this.left = 0;
        this.right = resolution.getScaledWidth();
        parent.setWidth(resolution.getScaledWidth());
        parent.setHeight(resolution.getScaledHeight());
        parent.init();
    }

    public void setText(String newChatText, boolean shouldOverwrite) {
        parent.setText(newChatText, shouldOverwrite);
    }

    public void changeSideMargin(SidebarSide side, int sidebarWidth) {
        this.changeSideMargin(
                side == SidebarSide.LEFT ? sidebarWidth : 0,
                side == SidebarSide.LEFT ? parent.getWidth() : parent.getWidth() - sidebarWidth
        );
    }

    public void changeSideMargin(int left, int right) {
        this.left = left;
        this.right = right;
        parent.setInputFieldX(left + 4);
        parent.setInputFieldWidth(right - left - 4);
    }

    public void drawScreen(double mouseX, double mouseY, float partialTicks) {
        GuiStaticConnector.INSTANCE.drawRect(
                left + 2, parent.getHeight() - 14,
                right - 2, parent.getHeight() - 2, Integer.MIN_VALUE
        );
        parent.drawInputFieldBox();
        parent.handleMouseHover(mouseX, mouseY, partialTicks);
    }

    public boolean keyTypedResponse(char typedChar, int keyCode) {
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
        parent.updateScreen();
    }

    public void handleMouseInput() throws IOException {
        parent.handleMouseInput();
    }
}
