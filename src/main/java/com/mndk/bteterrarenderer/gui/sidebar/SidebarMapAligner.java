package com.mndk.bteterrarenderer.gui.sidebar;

import com.mndk.bteterrarenderer.gui.components.GuiNumberInput;
import com.mndk.bteterrarenderer.util.GetterSetter;

import java.io.IOException;

public class SidebarMapAligner extends GuiSidebarElement {



    private static final int GRAY_AREA_HEIGHT = 40;

    private static final int TEXT_AND_GRAY_AREA_DIST = 10;


    private GuiNumberInput xInput, zInput;
    private final GetterSetter<Double> xOffset, zOffset;


    public SidebarMapAligner(GetterSetter<Double> xOffset, GetterSetter<Double> zOffset) {
        this.xOffset = xOffset;
        this.zOffset = zOffset;
    }


    @Override
    protected void init() {
        this.xInput = new GuiNumberInput(
                -1, fontRenderer,
                0, 0, parent.elementWidth.get() / 2 - 3, 20,
                xOffset, "X ="
        );
        this.zInput = new GuiNumberInput(
                -1, fontRenderer,
                parent.elementWidth.get() / 2 + 3, 0, parent.elementWidth.get() / 2 - 3, 20,
                zOffset, "Z ="
        );
    }


    @Override
    public int getHeight() {
        return 20 + TEXT_AND_GRAY_AREA_DIST + GRAY_AREA_HEIGHT;
    }


    @Override
    public void onWidthChange(int newWidth) {
        xInput.setWidth(newWidth / 2 - 3);
        zInput.setWidth(newWidth / 2 - 3);
        zInput.setX(newWidth / 2 + 3);
    }


    @Override
    public void updateScreen() {}


    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        xInput.drawTextBox(); zInput.drawTextBox();
    }


    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        xInput.mouseClicked(mouseX, mouseY, mouseButton);
        zInput.mouseClicked(mouseX, mouseY, mouseButton);
        return false;
    }


    @Override
    public void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {

    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {

    }

    @Override
    public void keyTyped(char key, int keyCode) throws IOException {
        xInput.textboxKeyTyped(key, keyCode); zInput.textboxKeyTyped(key, keyCode);
    }
}
