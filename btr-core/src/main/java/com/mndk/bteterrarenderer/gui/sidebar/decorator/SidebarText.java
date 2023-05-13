package com.mndk.bteterrarenderer.gui.sidebar.decorator;

import com.mndk.bteterrarenderer.connector.gui.FontRendererConnector;
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
        this.formattedStringList = FontRendererConnector.INSTANCE.listFormattedStringToWidth(displayString, parent.elementWidth.get());
    }

    @Override
    public void onWidthChange(int newWidth) {
        this.formattedStringList = FontRendererConnector.INSTANCE.listFormattedStringToWidth(displayString, parent.elementWidth.get());
    }

    @Override
    public int getHeight() {
        return formattedStringList.size() * FontRendererConnector.INSTANCE.getFontHeight();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {

        for(int i = 0; i < formattedStringList.size(); ++i) {
            String line = formattedStringList.get(i);
            if(align == TextAlign.LEFT) {
                FontRendererConnector.INSTANCE.drawStringWithShadow(
                        line,
                        0, i * FontRendererConnector.INSTANCE.getFontHeight(), 0xFFFFFF
                );
            }
            else if(align == TextAlign.RIGHT) {
                FontRendererConnector.INSTANCE.drawStringWithShadow(
                        line,
                        parent.elementWidth.get() - FontRendererConnector.INSTANCE.getStringWidth(line),
                        i * FontRendererConnector.INSTANCE.getFontHeight(), 0xFFFFFF
                );
            }
            else if(align == TextAlign.CENTER){
                FontRendererConnector.INSTANCE.drawCenteredStringWithShadow(
                        line,
                        parent.elementWidth.get() / 2f, i * FontRendererConnector.INSTANCE.getFontHeight(), 0xFFFFFF
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
