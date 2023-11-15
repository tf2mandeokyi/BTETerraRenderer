package com.mndk.bteterrarenderer.core.gui.sidebar.mapaligner;

import com.mndk.bteterrarenderer.core.graphics.GlGraphicsManager;
import com.mndk.bteterrarenderer.core.graphics.GraphicsQuad;
import com.mndk.bteterrarenderer.core.util.mixin.MixinDelegateCreator;
import com.mndk.bteterrarenderer.core.util.minecraft.MinecraftClientManager;
import com.mndk.bteterrarenderer.core.BTETerraRendererConstants;
import com.mndk.bteterrarenderer.core.gui.RawGuiManager;
import com.mndk.bteterrarenderer.core.gui.components.GuiCheckBoxCopy;
import com.mndk.bteterrarenderer.core.gui.components.GuiNumberInput;
import com.mndk.bteterrarenderer.core.gui.sidebar.GuiSidebarElement;
import com.mndk.bteterrarenderer.core.util.accessor.PropertyAccessor;
import com.mndk.bteterrarenderer.core.util.mixin.delegate.IResourceLocation;
import com.mndk.bteterrarenderer.core.input.InputKey;
import com.mndk.bteterrarenderer.core.util.i18n.I18nManager;

public class SidebarMapAligner extends GuiSidebarElement {

    private static final int ALIGNBOX_MARGIN_VERT = 10;
    private static final int ALIGNBOX_MARGIN_SIDE = 15;
    private static final int ALIGNBOX_HEIGHT = 150;

    private static final double MOUSE_DIVIDER = 30;

    private static final int ALIGNBOX_BACKGROUND_COLOR = 0x90000000;
    private static final int AXIS_LINE_COLOR = 0xFFFF0000;
    private static final int PRIMARY_LINE_COLOR = 0xFFFFFFFF;
    private static final int SECONDARY_LINE_COLOR = 0xFF8E8E8E;
    private static final int LINE_LENGTH = 1000;

    private static final IResourceLocation ALIGNMENT_MARKER = MixinDelegateCreator.newResourceLocation(
            BTETerraRendererConstants.MODID, "textures/ui/alignment_marker.png"
    );

    private GuiNumberInput xInput, zInput;
    private final PropertyAccessor<Double> xOffset, zOffset;

    private GuiCheckBoxCopy lockNorthCheckBox;
    private final PropertyAccessor<Boolean> lockNorth;

    private double playerYawRadians;
    private boolean aligningMode;

    public SidebarMapAligner(
            PropertyAccessor<Double> xOffset, PropertyAccessor<Double> zOffset, PropertyAccessor<Boolean> lockNorth
    ) {
        this.xOffset = xOffset;
        this.zOffset = zOffset;
        this.lockNorth = lockNorth;
        this.playerYawRadians = 0;
        this.aligningMode = false;
    }

    @Override
    protected void init() {
        int width = parent.elementWidth.get().intValue();
        this.xInput = new GuiNumberInput(
                0, 0, width / 2 - 3, 20,
                xOffset, "X = "
        );
        this.zInput = new GuiNumberInput(
                width / 2 + 3, 0, width / 2 - 3, 20,
                zOffset, "Z = "
        );
        this.lockNorthCheckBox = new GuiCheckBoxCopy(
                ALIGNBOX_MARGIN_SIDE, 20 + ALIGNBOX_MARGIN_VERT * 2 + ALIGNBOX_HEIGHT,
                I18nManager.format("gui.bteterrarenderer.settings.lock_north"), this.lockNorth.get()
        );
    }

    private void setPlayerYawRadians() {
        this.playerYawRadians = lockNorthCheckBox.isChecked() ?
                Math.PI :
                Math.toRadians(MinecraftClientManager.getPlayerRotationYaw());
    }

    @Override
    public void tick() {
        xInput.tick();
        zInput.tick();
    }

    @Override
    public int getPhysicalHeight() {
        return 40 + ALIGNBOX_MARGIN_VERT * 2 + ALIGNBOX_HEIGHT;
    }

    @Override
    public void onWidthChange(double newWidth) {
        this.xInput.setWidth((int) (newWidth / 2 - 3));
        this.zInput.setWidth((int) (newWidth / 2 - 3));
        this.zInput.setX((int) (newWidth / 2 + 3));
    }

    @Override
    public boolean mouseHovered(double mouseX, double mouseY, float partialTicks, boolean mouseHidden) {
        boolean result = xInput.mouseHovered(mouseX, mouseY, partialTicks, mouseHidden);
        if(zInput.mouseHovered(mouseX, mouseY, partialTicks, mouseHidden)) result = true;
        if(lockNorthCheckBox.mouseHovered(mouseX, mouseY, partialTicks, mouseHidden)) result = true;
        return result;
    }

    @Override
    public void drawComponent(Object poseStack) {
        xInput.drawComponent(poseStack);
        zInput.drawComponent(poseStack);
        lockNorthCheckBox.drawComponent(poseStack);

        this.setPlayerYawRadians();
        this.drawAlignBox(poseStack);
    }

    private void drawAlignBox(Object poseStack) {
        // TODO v1.03.1: Add numbers

        int elementWidth = parent.elementWidth.get().intValue();
        int centerX = elementWidth / 2, centerY = 20 + ALIGNBOX_MARGIN_VERT + ALIGNBOX_HEIGHT / 2;

        int boxN = 20 + ALIGNBOX_MARGIN_VERT, boxS = boxN + ALIGNBOX_HEIGHT;
        int boxW = ALIGNBOX_MARGIN_SIDE, boxE = elementWidth - ALIGNBOX_MARGIN_SIDE;

        // Box background
        RawGuiManager.fillRect(poseStack, boxW, boxN, boxE, boxS, ALIGNBOX_BACKGROUND_COLOR);

        // Enable box clipping
        if(GlGraphicsManager.glEnableRelativeScissor(poseStack, boxW, boxN, boxE - boxW, boxS - boxN)) {
            this.drawAlignBoxGrids(poseStack, boxW, boxN, boxE - boxW, boxS - boxN);
        }
        // Disable box clipping
        GlGraphicsManager.glDisableScissorTest();

        // Borders
//        int borderColor = this.alignBoxHovered ? HOVERED_COLOR : NORMAL_BORDER_COLOR;
//        if(aligningMode) borderColor = FOCUSED_BORDER_COLOR;
//
//        GuiStaticConnector.INSTANCE.fillRect(poseStack, boxW - 1, boxN - 1, boxW, boxS + 1, borderColor);
//        GuiStaticConnector.INSTANCE.fillRect(poseStack, boxW - 1, boxN - 1, boxE + 1, boxN, borderColor);
//        GuiStaticConnector.INSTANCE.fillRect(poseStack, boxE, boxN - 1, boxE + 1, boxS + 1, borderColor);
//        GuiStaticConnector.INSTANCE.fillRect(poseStack, boxW - 1, boxS, boxE + 1, boxS + 1, borderColor);

        // Center marker
        RawGuiManager.drawWholeCenteredImage(poseStack, ALIGNMENT_MARKER, centerX, centerY, 4, 4);
    }

    private void drawAlignBoxGrids(Object poseStack, int boxX, int boxY, int boxWidth, int boxHeight) {
        double alignX = this.xOffset.get(), alignZ = this.zOffset.get();
        int xi = (int) alignX, zi = (int) alignZ;
        int lineCount = (int) Math.ceil((boxWidth + ALIGNBOX_HEIGHT) / MOUSE_DIVIDER / 2);
        int centerX = boxX + boxWidth / 2, centerY = boxY + boxHeight / 2;

//        GraphicsQuad<GraphicsQuad.PosXY> box = GraphicsQuad.newPosXYQuad(
//                new GraphicsQuad.PosXY(boxX, boxY),
//                new GraphicsQuad.PosXY(boxX + boxWidth, boxY),
//                new GraphicsQuad.PosXY(boxX + boxWidth, boxY + boxHeight),
//                new GraphicsQuad.PosXY(boxX, boxY + boxHeight)
//        );

        // Secondary lines
        for(int z = zi - lineCount; z <= zi + lineCount; z++) {
            if(z % 5 == 0) continue;
            drawNorthSouthLine(poseStack, centerX, centerY, z, SECONDARY_LINE_COLOR);
        }
        for(int x = xi - lineCount; x <= xi + lineCount; x++) {
            if(x % 5 == 0) continue;
            drawEastWestLine(poseStack, centerX, centerY, x, SECONDARY_LINE_COLOR);
        }

        // Primary lines
        for(int z = zi - lineCount; z <= zi + lineCount; z++) {
            if(z % 5 != 0 || z == 0) continue;
            drawNorthSouthLine(poseStack, centerX, centerY, z, PRIMARY_LINE_COLOR);
        }
        for(int x = xi - lineCount; x <= xi + lineCount; x++) {
            if(x % 5 != 0 || x == 0) continue;
            drawEastWestLine(poseStack, centerX, centerY, x, PRIMARY_LINE_COLOR);
        }

        // Axis lines
        drawNorthSouthLine(poseStack, centerX, centerY, 0, AXIS_LINE_COLOR);
        drawEastWestLine(poseStack, centerX, centerY, 0, AXIS_LINE_COLOR);
    }

    private void drawNorthSouthLine(Object poseStack,
                                    int centerX, int centerY,
                                    int zIndex, int color) {
        double alignZ = this.zOffset.get();
        double dx = Math.cos(playerYawRadians), dy = -Math.sin(playerYawRadians);

        double diff = (zIndex - alignZ) * MOUSE_DIVIDER;
        double diffX = dy * diff, diffY = -dx * diff;

        drawLine(poseStack,
                centerX + diffX - dx * LINE_LENGTH, centerY + diffY - dy * LINE_LENGTH,
                dx * 2*LINE_LENGTH, dy * 2*LINE_LENGTH,
                color
        );
    }

    private void drawEastWestLine(Object poseStack,
                                  int centerX, int centerY,
                                  int xIndex, int color) {
        double alignX = this.xOffset.get();
        double dx = Math.cos(playerYawRadians), dy = -Math.sin(playerYawRadians);

        double diff = (alignX - xIndex) * MOUSE_DIVIDER;
        double diffX = dx * diff, diffY = dy * diff;

        drawLine(poseStack,
                centerX + diffX - dy * LINE_LENGTH, centerY + diffY + dx * LINE_LENGTH,
                dy * 2*LINE_LENGTH, -dx * 2*LINE_LENGTH,
                color
        );
    }

    private void drawLine(Object poseStack,
                          double x, double y, double dx, double dy,
                          int color) {
        GraphicsQuad<GraphicsQuad.PosXY> line = RawGuiManager.makeLineDxDy(x, y, dx, dy, 1);
        if(line == null) return;
        RawGuiManager.fillQuad(poseStack, line, color, 0);
    }

    @Override
    public boolean mousePressed(double mouseX, double mouseY, int mouseButton) {
        xInput.mousePressed(mouseX, mouseY, mouseButton);
        zInput.mousePressed(mouseX, mouseY, mouseButton);

        if(mouseButton == 0) {
            if(lockNorthCheckBox.mousePressed(mouseX, mouseY, mouseButton)) {
                lockNorth.set(lockNorthCheckBox.isChecked());
                this.setPlayerYawRadians();
                return true;
            }
            if(mouseInAlignBox(mouseX, mouseY)) {
                aligningMode = true;
            }
        }

        return false;
    }

    private boolean mouseInAlignBox(double mouseX, double mouseY) {
        int width = parent.elementWidth.get().intValue();
        return mouseX >= 10 && mouseX <= width - 10 &&
                mouseY >= 20 + ALIGNBOX_MARGIN_VERT && mouseY <= 20 + ALIGNBOX_MARGIN_VERT + ALIGNBOX_HEIGHT;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double pMouseX, double pMouseY) {
        if(aligningMode) {
            double dmx = (mouseX - pMouseX) / MOUSE_DIVIDER, dmy = (mouseY - pMouseY) / MOUSE_DIVIDER;

            double dx = dmx * Math.cos(playerYawRadians) - dmy * Math.sin(playerYawRadians);
            double dz = dmx * Math.sin(playerYawRadians) + dmy * Math.cos(playerYawRadians);

            this.xOffset.set(dx + this.xOffset.get());
            this.zOffset.set(dz + this.zOffset.get());

            this.xInput.update(); this.zInput.update();
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int state) {
        this.aligningMode = false;
        return true;
    }

    @Override
    public boolean keyTyped(char typedChar, int keyCode) {
        boolean result = xInput.keyTyped(typedChar, keyCode);
        if(zInput.keyTyped(typedChar, keyCode)) result = true;
        return result;
    }

    @Override
    public boolean keyPressed(InputKey key) {
        boolean result = xInput.keyPressed(key);
        if(zInput.keyPressed(key)) result = true;
        return result;
    }
}
