package com.mndk.bteterrarenderer.gui.sidebar;

import java.util.List;

public class SidebarText extends GuiSidebarElement {

    public final String displayString;
    public final int color;
    private List<String> formattedStringList;

    public SidebarText(String displayString, int color) {
        this.displayString = displayString;
        this.color = color;
    }

    public SidebarText(String displayString) {
        this(displayString, 0xFFFFFF);
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
        return formattedStringList.size() * fontRenderer.FONT_HEIGHT;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {

        for(int i = 0; i < formattedStringList.size(); ++i) {
            String line = formattedStringList.get(i);
            this.drawCenteredString(
                    fontRenderer, line,
                    parent.elementWidth.get() / 2, i * fontRenderer.FONT_HEIGHT, 0xFFFFFF
            );
        }
    }


    @Override public void updateScreen() {}
    @Override public void mouseClicked(int mouseX, int mouseY, int mouseButton) {}
    @Override public void keyTyped(char key, int keyCode) {}
    @Override public void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {}
    @Override public void mouseReleased(int mouseX, int mouseY, int state) {}
}
