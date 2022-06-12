package com.mndk.bteterrarenderer.gui.sidebar;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.input.Mouse;

import javax.annotation.Nonnull;
import java.io.IOException;

public class SidebarGuiChat extends GuiChat {

    @Getter @Setter
    private boolean opened;

    private int left, right;

    public SidebarGuiChat() {
        super();
        this.opened = false;
    }


    public void initGui(Minecraft mc, ScaledResolution resolution) {
        this.mc = mc;
        this.itemRender = mc.getRenderItem();
        this.fontRenderer = mc.fontRenderer;
        this.width = resolution.getScaledWidth();
        this.height = resolution.getScaledHeight();
        this.left = 0;
        this.right = this.width;
        super.initGui();
    }


    @Override
    public void setText(@Nonnull String newChatText, boolean shouldOverwrite) {
        super.setText(newChatText, shouldOverwrite);
    }


    public void changeSideMargin(SidebarSide side, int sidebarWidth) {
        this.changeSideMargin(
                side == SidebarSide.LEFT ? sidebarWidth : 0,
                side == SidebarSide.LEFT ? this.width : this.width - sidebarWidth
        );
    }


    public void changeSideMargin(int left, int right) {
        this.left = left;
        this.right = right;
        this.inputField.x = left + 4;
        this.inputField.width = right - left - 4;
    }


    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        drawRect(left + 2, this.height - 14, right - 2, this.height - 2, Integer.MIN_VALUE);
        this.inputField.drawTextBox();
        ITextComponent itextcomponent = this.mc.ingameGUI.getChatGUI().getChatComponent(Mouse.getX(), Mouse.getY());

        if (itextcomponent != null && itextcomponent.getStyle().getHoverEvent() != null) {
            this.handleComponentHover(itextcomponent, mouseX, mouseY);
        }
    }


    public boolean keyTypedResponse(char typedChar, int keyCode) throws IOException {
        if(!this.inputField.isFocused()) return false;

        if (keyCode == 28 || keyCode == 156) {
            String s = this.inputField.getText().trim();
            if (!s.isEmpty()) this.sendChatMessage(s);
            this.opened = false;
            return true;
        }
        super.keyTyped(typedChar, keyCode);
        return true;
    }


    public boolean mouseClickResponse(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        if (mouseButton == 0) {
            ITextComponent itextcomponent = this.mc.ingameGUI.getChatGUI().getChatComponent(Mouse.getX(), Mouse.getY());
            if (itextcomponent != null) return this.handleComponentClick(itextcomponent);
        }

        return this.inputField.mouseClicked(mouseX, mouseY, mouseButton);
    }

}
