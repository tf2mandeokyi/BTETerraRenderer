package com.mndk.bteterrarenderer.gui.sidebar.mapaligner;

import com.mndk.bteterrarenderer.gui.components.GuiNumberInput;
import com.mndk.bteterrarenderer.gui.sidebar.GuiSidebarElement;
import com.mndk.bteterrarenderer.util.GetterSetter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

public class SidebarMapAligner extends GuiSidebarElement {


    private static final int TEXT_AND_GRAY_AREA_DIST = 10;

    private static final int ALIGNBOX_HEIGHT = 100;
    private static final double MOUSE_MULTIPLIER = 0.05;


    private GuiNumberInput xInput, zInput;
    private final GetterSetter<Double> xOffset, zOffset;
    private int pMouseX = -1, pMouseY = -1;
    private double playerRotationRadians;
    private boolean aligningMode;


    public SidebarMapAligner(GetterSetter<Double> xOffset, GetterSetter<Double> zOffset) {
        this.xOffset = xOffset;
        this.zOffset = zOffset;
        this.playerRotationRadians = 0;
        this.aligningMode = false;
    }


    @Override
    protected void init() {
        int width = parent.elementWidth.get();
        this.xInput = new GuiNumberInput(
                -1, fontRenderer,
                0, 0, width / 2 - 3, 20,
                xOffset, "X = "
        );
        this.zInput = new GuiNumberInput(
                -1, fontRenderer,
                width / 2 + 3, 0, width / 2 - 3, 20,
                zOffset, "Z = "
        );
        this.playerRotationRadians = Math.toRadians(Minecraft.getMinecraft().player.rotationYaw);
    }


    @Override
    public int getHeight() {
        return 20 + TEXT_AND_GRAY_AREA_DIST + ALIGNBOX_HEIGHT;
    }


    @Override
    public void onWidthChange(int newWidth) {
        this.xInput.setWidth(newWidth / 2 - 3);
        this.zInput.setWidth(newWidth / 2 - 3);
        this.zInput.setX(newWidth / 2 + 3);
    }


    @Override
    public void updateScreen() {}


    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        xInput.drawTextBox(); zInput.drawTextBox();

        Gui.drawRect(
                10, 20 + TEXT_AND_GRAY_AREA_DIST,
                parent.elementWidth.get() - 10, 20 + TEXT_AND_GRAY_AREA_DIST + ALIGNBOX_HEIGHT,
                0xFFCCCCCC
        );
    }


    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        xInput.mouseClicked(mouseX, mouseY, mouseButton);
        zInput.mouseClicked(mouseX, mouseY, mouseButton);
        if(isMouseInAlignBox(mouseX, mouseY) && mouseButton == 0) {
            aligningMode = true;
            pMouseX = mouseX; pMouseY = mouseY;
        }
        return false;
    }


    private boolean isMouseInAlignBox(int mouseX, int mouseY) {
        int width = parent.elementWidth.get();
        return mouseX >= 10 && mouseX <= width - 10 &&
                mouseY >= 20 + TEXT_AND_GRAY_AREA_DIST && mouseY <= 20 + TEXT_AND_GRAY_AREA_DIST + ALIGNBOX_HEIGHT;
    }


    @Override
    public void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if(aligningMode) {
            double dmx = MOUSE_MULTIPLIER * (mouseX - this.pMouseX), dmy = MOUSE_MULTIPLIER * (mouseY - this.pMouseY);

            double dx = dmx * Math.cos(playerRotationRadians) - dmy * Math.sin(playerRotationRadians);
            double dz = dmx * Math.sin(playerRotationRadians) + dmy * Math.cos(playerRotationRadians);

            this.xOffset.set(dx + this.xOffset.get());
            this.zOffset.set(dz + this.zOffset.get());

            this.xInput.update(); this.zInput.update();

            this.pMouseX = mouseX; this.pMouseY = mouseY;
        }
    }


    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        this.aligningMode = false;
    }


    @Override
    public boolean keyTyped(char key, int keyCode) {
        boolean result = xInput.textboxKeyTyped(key, keyCode);
        if(zInput.textboxKeyTyped(key, keyCode)) result = true;
        return result;
    }
}
