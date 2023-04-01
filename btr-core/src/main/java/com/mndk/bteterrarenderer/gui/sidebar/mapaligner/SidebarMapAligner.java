package com.mndk.bteterrarenderer.gui.sidebar.mapaligner;

import com.mndk.bteterrarenderer.BTETerraRendererCore;
import com.mndk.bteterrarenderer.connector.Connectors;
import com.mndk.bteterrarenderer.connector.minecraft.ResourceLocationConnector;
import com.mndk.bteterrarenderer.connector.minecraft.gui.GuiCheckBoxConnector;
import com.mndk.bteterrarenderer.gui.components.GuiNumberInput;
import com.mndk.bteterrarenderer.gui.sidebar.GuiSidebarElement;
import com.mndk.bteterrarenderer.util.GetterSetter;
import com.mndk.bteterrarenderer.util.gui.GuiUtils;

public class SidebarMapAligner extends GuiSidebarElement {


    private static final int ALIGNBOX_MARGIN_VERT = 10;
    private static final int ALIGNBOX_MARGIN_SIDE = 15;
    private static final int ALIGNBOX_HEIGHT = 150;

    private static final double MOUSE_DIVIDER = 30;

    private static final ResourceLocationConnector ALIGNMENT_MARKER = Connectors.SUPPLIER.newResourceLocation(
            BTETerraRendererCore.MODID, "textures/ui/alignment_marker.png"
    );

    private GuiNumberInput xInput, zInput;
    private final GetterSetter<Double> xOffset, zOffset;

    private GuiCheckBoxConnector lockNorthCheckBox;
    private final GetterSetter<Boolean> lockNorth;

    private int pMouseX = -1, pMouseY = -1;
    private double playerYawRadians;
    private boolean aligningMode;

    public SidebarMapAligner(
            GetterSetter<Double> xOffset, GetterSetter<Double> zOffset, GetterSetter<Boolean> lockNorth
    ) {
        this.xOffset = xOffset;
        this.zOffset = zOffset;
        this.lockNorth = lockNorth;
        this.playerYawRadians = 0;
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
        this.lockNorthCheckBox = Connectors.SUPPLIER.newCheckBox(
                -1,
                ALIGNBOX_MARGIN_SIDE, 20 + ALIGNBOX_MARGIN_VERT * 2 + ALIGNBOX_HEIGHT,
                Connectors.I18N.format("gui.bteterrarenderer.new_settings.lock_north"), this.lockNorth.get()
        );
        this.setPlayerYawRadians();
    }

    private void setPlayerYawRadians() {
        this.playerYawRadians = lockNorthCheckBox.isChecked() ?
                Math.PI :
                Math.toRadians(Connectors.PLAYER.getRotationYaw());
    }

    @Override
    public int getHeight() {
        return 40 + ALIGNBOX_MARGIN_VERT * 2 + ALIGNBOX_HEIGHT;
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
        lockNorthCheckBox.drawButton(mouseX, mouseY, partialTicks);
        this.drawAlignBox();
    }

    private void drawAlignBox() {
        int elementWidth = parent.elementWidth.get(), boxWidth = elementWidth - ALIGNBOX_MARGIN_SIDE * 2;
        double alignX = this.xOffset.get(), alignZ = this.zOffset.get();
        int xi = (int) alignX, zi = (int) alignZ;
        int lineCount = (int) Math.ceil((boxWidth + ALIGNBOX_HEIGHT) / MOUSE_DIVIDER / 2);
        double dx = Math.cos(playerYawRadians), dy = -Math.sin(playerYawRadians);
        int centerX = elementWidth / 2, centerY = 20 + ALIGNBOX_MARGIN_VERT + ALIGNBOX_HEIGHT / 2;

        // Enable box clipping
        Connectors.GRAPHICS.glEnableScissorTest();
        Connectors.GRAPHICS.glRelativeScissor(
                ALIGNBOX_MARGIN_SIDE, 20 + ALIGNBOX_MARGIN_VERT,
                parent.elementWidth.get() - 2 * ALIGNBOX_MARGIN_SIDE, ALIGNBOX_HEIGHT
        );

        Connectors.GUI.drawRect(
                ALIGNBOX_MARGIN_SIDE, 20 + ALIGNBOX_MARGIN_VERT,
                elementWidth - ALIGNBOX_MARGIN_SIDE, 20 + ALIGNBOX_MARGIN_VERT + ALIGNBOX_HEIGHT,
                0xBB000000
        );

        // North-South lines
        for(int z = zi - lineCount; z <= zi + lineCount; z++) {
            double diff = (z - alignZ) * MOUSE_DIVIDER;
            double diffX = dy * diff, diffY = -dx * diff;
            int color = z == 0 ? 0xFFFF0000 : (z % 5 == 0 ? 0xFFFFFFFF : 0xFF8E8E8E);

            GuiUtils.drawLineDxDy(
                    centerX + diffX - dx * 1000, centerY + diffY - dy * 1000,
                    dx * 2000, dy * 2000,
                    1, color
            );
        }
        // East-West lines
        for(int x = xi - lineCount; x <= xi + lineCount; x++) {
            double diff = (alignX - x) * MOUSE_DIVIDER;
            double diffX = dx * diff, diffY = dy * diff;
            int color = x == 0 ? 0xFFFF0000 : (x % 5 == 0 ? 0xFFFFFFFF : 0xFF8E8E8E);

            GuiUtils.drawLineDxDy(
                    centerX + diffX - dy * 1000, centerY + diffY + dx * 1000,
                    dy * 2000, -dx * 2000,
                    1, color
            );
        }

        // Disable box clipping
        Connectors.GRAPHICS.glDisableScissorTest();

        // Center marker
        GuiUtils.drawCenteredImage(ALIGNMENT_MARKER, centerX, centerY, 0, 4, 4);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        xInput.mouseClicked(mouseX, mouseY, mouseButton);
        zInput.mouseClicked(mouseX, mouseY, mouseButton);

        if(mouseButton == 0) {
            if(lockNorthCheckBox.mousePressed(mouseX, mouseY)) {
                lockNorth.set(lockNorthCheckBox.isChecked());
                this.setPlayerYawRadians();
                return true;
            }
            if(isMouseInAlignBox(mouseX, mouseY)) {
                aligningMode = true;
                pMouseX = mouseX; pMouseY = mouseY;
            }
        }

        return false;
    }

    private boolean isMouseInAlignBox(int mouseX, int mouseY) {
        int width = parent.elementWidth.get();
        return mouseX >= 10 && mouseX <= width - 10 &&
                mouseY >= 20 + ALIGNBOX_MARGIN_VERT && mouseY <= 20 + ALIGNBOX_MARGIN_VERT + ALIGNBOX_HEIGHT;
    }

    @Override
    public void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if(aligningMode) {
            double dmx = (mouseX - this.pMouseX) / MOUSE_DIVIDER, dmy = (mouseY - this.pMouseY) / MOUSE_DIVIDER;

            double dx = dmx * Math.cos(playerYawRadians) - dmy * Math.sin(playerYawRadians);
            double dz = dmx * Math.sin(playerYawRadians) + dmy * Math.cos(playerYawRadians);

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
