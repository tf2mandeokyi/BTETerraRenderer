package com.mndk.bteterrarenderer.gui.sidebar.decorator;

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
        this.formattedStringList = fontRenderer.listFormattedStringToWidth(displayString, parent.elementWidth.get());
    }

    @Override
    public void onWidthChange(int newWidth) {
        this.formattedStringList = fontRenderer.listFormattedStringToWidth(displayString, parent.elementWidth.get());
    }

    @Override
    public int getHeight() {
        return formattedStringList.size() * fontRenderer.getFontHeight();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {

        for(int i = 0; i < formattedStringList.size(); ++i) {
            String line = formattedStringList.get(i);
            if(align == TextAlign.LEFT) {
                fontRenderer.drawStringWithShadow(
                        line,
                        0, i * fontRenderer.getFontHeight(), 0xFFFFFF
                );
            }
            else if(align == TextAlign.RIGHT) {
                fontRenderer.drawStringWithShadow(
                        line,
                        parent.elementWidth.get() - fontRenderer.getStringWidth(line),
                        i * fontRenderer.getFontHeight(), 0xFFFFFF
                );
            }
            else if(align == TextAlign.CENTER){
                fontRenderer.drawCenteredStringWithShadow(
                        line,
                        parent.elementWidth.get() / 2f, i * fontRenderer.getFontHeight(), 0xFFFFFF
                );
            }
        }
    }


    @Override public void updateScreen() {}
    @Override public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) { return false; }
    @Override public boolean keyTyped(char key, int keyCode) { return false; }
    @Override public void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {}
    @Override public void mouseReleased(int mouseX, int mouseY, int state) {}

    public enum TextAlign {
        LEFT, CENTER, RIGHT
    }
}
