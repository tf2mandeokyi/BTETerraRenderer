package com.mndk.bteterrarenderer.gui.sidebar.decorator;

import com.mndk.bteterrarenderer.connector.gui.FontConnector;
import com.mndk.bteterrarenderer.gui.sidebar.GuiSidebarElement;

import java.util.List;

public class SidebarText extends GuiSidebarElement {

    public final String displayString;
    public final TextAlign align;
    public final int color;
    private List<String> formattedStringList;

    public SidebarText(String displayString, TextAlign align, int color) {
        this.displayString = displayString;
        this.color = color;
        this.align = align;
    }

    public SidebarText(String displayString, TextAlign align) {
        this(displayString, align, 0xFFFFFF);
    }

    @Override
    protected void init() {
        this.formattedStringList = FontConnector.INSTANCE.listFormattedStringToWidth(displayString, parent.elementWidth.get());
    }

    @Override
    public void onWidthChange(int newWidth) {
        this.formattedStringList = FontConnector.INSTANCE.listFormattedStringToWidth(displayString, parent.elementWidth.get());
    }

    @Override
    public int getHeight() {
        return formattedStringList.size() * FontConnector.INSTANCE.getFontHeight();
    }

    @Override
    public void drawComponent(double mouseX, double mouseY, float partialTicks) {

        for(int i = 0; i < formattedStringList.size(); ++i) {
            String line = formattedStringList.get(i);
            if(align == TextAlign.LEFT) {
                FontConnector.INSTANCE.drawStringWithShadow(
                        line,
                        0, i * FontConnector.INSTANCE.getFontHeight(), 0xFFFFFF
                );
            }
            else if(align == TextAlign.RIGHT) {
                FontConnector.INSTANCE.drawStringWithShadow(
                        line,
                        parent.elementWidth.get() - FontConnector.INSTANCE.getStringWidth(line),
                        i * FontConnector.INSTANCE.getFontHeight(), 0xFFFFFF
                );
            }
            else if(align == TextAlign.CENTER){
                FontConnector.INSTANCE.drawCenteredStringWithShadow(
                        line,
                        parent.elementWidth.get() / 2f, i * FontConnector.INSTANCE.getFontHeight(), 0xFFFFFF
                );
            }
        }
    }


    @Override public void updateScreen() {}
    @Override public boolean mousePressed(double mouseX, double mouseY, int mouseButton) { return false; }
    @Override public boolean keyTyped(char typedChar, int keyCode) { return false; }
    @Override public void mouseDragged(double mouseX, double mouseY, int mouseButton, double pMouseX, double pMouseY) {}
    @Override public void mouseReleased(double mouseX, double mouseY, int state) {}

    public enum TextAlign {
        LEFT, CENTER, RIGHT
    }
}
