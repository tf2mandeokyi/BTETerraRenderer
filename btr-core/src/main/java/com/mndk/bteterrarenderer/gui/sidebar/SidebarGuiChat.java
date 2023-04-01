package com.mndk.bteterrarenderer.gui.sidebar;

import com.mndk.bteterrarenderer.connector.Connectors;
import com.mndk.bteterrarenderer.connector.minecraft.gui.GuiChatConnector;
import com.mndk.bteterrarenderer.connector.minecraft.graphics.ScaledResolutionConnector;
import lombok.Getter;
import lombok.Setter;
import java.io.IOException;

public class SidebarGuiChat {

    @Getter @Setter
    private boolean opened;
    private final GuiChatConnector parent;

    private int left, right;

    public SidebarGuiChat() {
        this.parent = Connectors.SUPPLIER.newGuiChat();
        this.opened = false;
    }


    public void initGui(ScaledResolutionConnector resolution) {
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

    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        Connectors.GUI.drawRect(
                left + 2, parent.getHeight() - 14,
                right - 2, parent.getHeight() - 2, Integer.MIN_VALUE
        );
        parent.drawInputFieldBox();
        parent.handleMouseHover(mouseX, mouseY, partialTicks);
    }

    public boolean keyTypedResponse(char typedChar, int keyCode) throws IOException {
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


    public boolean mouseClickResponse(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0) {
            parent.handleMouseClick(mouseX, mouseY, mouseButton);
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

    public void handleMouseInput() {
        parent.handleMouseInput();
    }
}
