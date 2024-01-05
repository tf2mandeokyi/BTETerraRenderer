package com.mndk.bteterrarenderer.core.gui.sidebar.mapaligner;

import com.mndk.bteterrarenderer.core.BTETerraRendererConstants;
import com.mndk.bteterrarenderer.core.gui.component.GuiNumberInput;
import com.mndk.bteterrarenderer.core.gui.sidebar.GuiSidebarElement;
import com.mndk.bteterrarenderer.core.util.StringUtil;
import com.mndk.bteterrarenderer.core.util.accessor.PropertyAccessor;
import com.mndk.bteterrarenderer.mcconnector.IResourceLocation;
import com.mndk.bteterrarenderer.mcconnector.client.MinecraftClientManager;
import com.mndk.bteterrarenderer.mcconnector.graphics.GlGraphicsManager;
import com.mndk.bteterrarenderer.mcconnector.graphics.format.PosXY;
import com.mndk.bteterrarenderer.mcconnector.graphics.shape.GraphicsQuad;
import com.mndk.bteterrarenderer.mcconnector.gui.FontRenderer;
import com.mndk.bteterrarenderer.mcconnector.gui.HorizontalAlign;
import com.mndk.bteterrarenderer.mcconnector.gui.RawGuiManager;
import com.mndk.bteterrarenderer.mcconnector.gui.VerticalAlign;
import com.mndk.bteterrarenderer.mcconnector.gui.component.GuiCheckBoxCopy;
import com.mndk.bteterrarenderer.mcconnector.i18n.I18nManager;
import com.mndk.bteterrarenderer.mcconnector.input.InputKey;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class SidebarMapAligner extends GuiSidebarElement {

    private static final int ALIGNBOX_MARGIN_TOP = 20;
    private static final int ALIGNBOX_MARGIN_BOTTOM = 15;
    private static final int ALIGNBOX_MARGIN_SIDE = 15;
    private static final int ALIGNBOX_HEIGHT = 150;
    private static final int ALIGNBOX_ARROW_SIZE = 7;

    private static final float MOUSE_DIVIDER = 30;

    private static final int MARKER_COLOR = 0xFFFF0000;
    private static final int ALIGNBOX_BACKGROUND_COLOR = 0x90000000;
    private static final int AXIS_LINE_COLOR = 0xFFFFFFFF;
    private static final int PRIMARY_LINE_COLOR = 0xFF8D8D8D;
    private static final int SECONDARY_LINE_COLOR = 0xFF4D4D4D;
    private static final int LINE_LENGTH = 1000;

    private static final IResourceLocation ALIGNMENT_MARKER = IResourceLocation.of(
            BTETerraRendererConstants.MODID, "textures/ui/alignment_marker.png"
    );

    private GuiNumberInput xInput, zInput;
    private final PropertyAccessor<Double> xOffset, zOffset;

    private GuiCheckBoxCopy lockNorthCheckBox;
    private final PropertyAccessor<Boolean> lockNorth;

    private double playerYawRadians;
    private boolean aligningMode;
    private final List<GridLine> gridLines = new ArrayList<>();

    public SidebarMapAligner(PropertyAccessor<Double> xOffset, PropertyAccessor<Double> zOffset,
                             PropertyAccessor<Boolean> lockNorth) {
        this.xOffset = xOffset;
        this.zOffset = zOffset;
        this.lockNorth = lockNorth;
        this.playerYawRadians = 0;
        this.aligningMode = false;
    }

    @Override
    protected void init() {
        this.xInput = new GuiNumberInput(
                0, 0, this.getWidth() / 2 - 3, 20,
                xOffset, "X: "
        );
        this.zInput = new GuiNumberInput(
                this.getWidth() / 2 + 3, 0, this.getWidth() / 2 - 3, 20,
                zOffset, "Z: "
        );
        this.xInput.setPrefixColor(MARKER_COLOR);
        this.zInput.setPrefixColor(MARKER_COLOR);
        this.lockNorthCheckBox = new GuiCheckBoxCopy(
                ALIGNBOX_MARGIN_SIDE, 20 + ALIGNBOX_MARGIN_TOP + ALIGNBOX_HEIGHT + ALIGNBOX_MARGIN_BOTTOM,
                I18nManager.format("gui.bteterrarenderer.settings.lock_north"), this.lockNorth.get()
        );
    }

    private void updatePlayerYawRadians() {
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
        return 40 + ALIGNBOX_MARGIN_TOP + ALIGNBOX_HEIGHT + ALIGNBOX_MARGIN_BOTTOM;
    }

    @Override
    public void onWidthChange() {
        this.xInput.setWidth(this.getWidth() / 2 - 3);
        this.zInput.setWidth(this.getWidth() / 2 - 3);
        this.zInput.setX(this.getWidth() / 2 + 3);
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

        this.updatePlayerYawRadians();
        this.drawAlignBox(poseStack);
    }

    private void drawAlignBox(Object poseStack) {
        int elementWidth = this.getWidth();
        int centerX = elementWidth / 2, centerY = 20 + ALIGNBOX_MARGIN_TOP + ALIGNBOX_HEIGHT / 2;

        int boxN = 20 + ALIGNBOX_MARGIN_TOP, boxS = boxN + ALIGNBOX_HEIGHT;
        int boxW = ALIGNBOX_MARGIN_SIDE, boxE = elementWidth - ALIGNBOX_MARGIN_SIDE;

        // Box background
        RawGuiManager.INSTANCE.fillRect(poseStack, boxW, boxN, boxE, boxS, ALIGNBOX_BACKGROUND_COLOR);

        // Grids
        this.updateGridLines();
        this.drawGridLines(poseStack, boxW, boxN, boxE - boxW, boxS - boxN);

        // Center marker
        RawGuiManager.INSTANCE.drawWholeCenteredImage(poseStack, ALIGNMENT_MARKER, centerX, centerY, 4, 4);
        String x = StringUtil.formatDoubleNicely(xOffset.get(), 2);
        String z = StringUtil.formatDoubleNicely(zOffset.get(), 2);
        String text = String.format("§rX §f%s§r, Z §f%s", x, z);
        FontRenderer.DEFAULT.drawStringWithShadow(poseStack, text, HorizontalAlign.LEFT, VerticalAlign.TOP, centerX+3, centerY+3, MARKER_COLOR);
    }

    private void drawGridLines(Object poseStack, int boxX, int boxY, int boxWidth, int boxHeight) {
        // Draw lines & arrows
        GlGraphicsManager.INSTANCE.pushRelativeScissor(poseStack, boxX, boxY, boxWidth, boxHeight);
        for(GridLine line : this.gridLines) {
            // line
            GraphicsQuad<PosXY> quad = RawGuiManager.INSTANCE.makeLineDxDy(line.x, line.y, line.dx, line.dy, 1);
            if (quad == null) return;
            RawGuiManager.INSTANCE.fillQuad(poseStack, quad, line.color, 0);

            if(line.intersectionParam == null) continue;
            double t = line.intersectionParam;
            float x = (float) (line.x + t * line.dx);
            float y = (float) (line.y + t * line.dy);

            // arrow
            if(line.arrowWidth == 0) continue;
            double distance = Math.sqrt(line.dx * line.dx + line.dy * line.dy);
            float dxnorm = (float) (line.dx / distance);
            float dynorm = (float) (line.dy / distance);
            float arrowBackX = x - dxnorm * ALIGNBOX_ARROW_SIZE;
            float arrowBackY = y - dynorm * ALIGNBOX_ARROW_SIZE;
            float sideDx =  dynorm * ALIGNBOX_ARROW_SIZE * line.arrowWidth;
            float sideDy = -dxnorm * ALIGNBOX_ARROW_SIZE * line.arrowWidth;
            RawGuiManager.INSTANCE.fillQuad(poseStack, GraphicsQuad.newPosXY(
                    new PosXY(x, y),
                    new PosXY(arrowBackX + sideDx, arrowBackY + sideDy),
                    new PosXY(arrowBackX - sideDx, arrowBackY - sideDy),
                    new PosXY(x, y)
            ), line.color, 0);
        }
        GlGraphicsManager.INSTANCE.popRelativeScissor();

        // Draw labels
        for(GridLine line : this.gridLines) {
            if(line.intersectionParam == null) continue;
            if(line.label == null) continue;
            double t = line.intersectionParam;
            float x = (float) (line.x + t * line.dx);
            float y = (float) (line.y + t * line.dy);
            FontRenderer.DEFAULT.drawStringWithShadow(poseStack, line.label, line.labelHAlign, line.labelVAlign, x, y, line.color);
        }
    }

    private void updateGridLines() {
        int elementWidth = this.getWidth();
        int boxN = 20 + ALIGNBOX_MARGIN_TOP, boxS = boxN + ALIGNBOX_HEIGHT;
        int boxW = ALIGNBOX_MARGIN_SIDE, boxE = elementWidth - ALIGNBOX_MARGIN_SIDE;
        int boxWidth = boxE - boxW, boxHeight = boxS - boxN;

        this.gridLines.clear();

        double alignX = this.xOffset.get(), alignZ = this.zOffset.get();
        int xi = (int) alignX, zi = (int) alignZ;
        int lineCount = (int) Math.ceil((boxWidth + ALIGNBOX_HEIGHT) / MOUSE_DIVIDER / 2);
        int centerX = boxW + boxWidth / 2, centerY = boxN + boxHeight / 2;

        // Secondary lines
        for(int z = zi - lineCount; z <= zi + lineCount; z++) {
            if(z % 5 == 0) continue;
            this.gridLines.add(makeNorthSouthLine(centerX, centerY, z, SECONDARY_LINE_COLOR, null, 0));
        }
        for(int x = xi - lineCount; x <= xi + lineCount; x++) {
            if(x % 5 == 0) continue;
            this.gridLines.add(makeEastWestLine(centerX, centerY, x, SECONDARY_LINE_COLOR, null, 0));
        }

        // Primary lines
        for(int z = zi - lineCount; z <= zi + lineCount; z++) {
            if(z % 5 != 0 || z == 0) continue;
            this.gridLines.add(makeNorthSouthLine(centerX, centerY, z, PRIMARY_LINE_COLOR, String.format(" Z(%d) ", z), 0.3f));
        }
        for(int x = xi - lineCount; x <= xi + lineCount; x++) {
            if(x % 5 != 0 || x == 0) continue;
            this.gridLines.add(makeEastWestLine(centerX, centerY, x, PRIMARY_LINE_COLOR, String.format(" X(%d) ", x), 0.3f));
        }

        // Axis lines
        this.gridLines.add(makeNorthSouthLine(centerX, centerY, 0, AXIS_LINE_COLOR, " Z(0) ", 0.5f));
        this.gridLines.add(makeEastWestLine(centerX, centerY, 0, AXIS_LINE_COLOR, " X(0) ", 0.5f));

        // Calculate arrow position & label alignments
        GridLine topLine    = new GridLine(boxW, boxN, boxWidth, 0, 0, null, 0);
        GridLine leftLine   = new GridLine(boxW, boxN, 0, boxHeight, 0, null, 0);
        GridLine bottomLine = new GridLine(boxW, boxN + boxHeight, boxWidth, 0, 0, null, 0);
        GridLine rightLine  = new GridLine(boxW + boxWidth, boxN, 0, boxHeight, 0, null, 0);
        for(GridLine line : this.gridLines) {
            Double t, maxT = null;
            HorizontalAlign hAlign = null;
            VerticalAlign vAlign = null;

            if((t = line.getIntersectionParameter(topLine))    != null)
            { maxT = t; hAlign = HorizontalAlign.CENTER; vAlign = VerticalAlign.BOTTOM; }
            if((t = line.getIntersectionParameter(leftLine))   != null && (maxT == null || t > maxT))
            { maxT = t; hAlign = HorizontalAlign.RIGHT;  vAlign = VerticalAlign.MIDDLE; }
            if((t = line.getIntersectionParameter(bottomLine)) != null && (maxT == null || t > maxT))
            { maxT = t; hAlign = HorizontalAlign.CENTER; vAlign = VerticalAlign.TOP;    }
            if((t = line.getIntersectionParameter(rightLine))  != null && (maxT == null || t > maxT))
            { maxT = t; hAlign = HorizontalAlign.LEFT;   vAlign = VerticalAlign.MIDDLE; }

            line.intersectionParam = maxT;
            line.labelHAlign = hAlign;
            line.labelVAlign = vAlign;
        }
    }

    private GridLine makeNorthSouthLine(int centerX, int centerY, int zIndex, int color, String name, float arrowWidth) {
        double alignZ = this.zOffset.get();
        double dx = Math.cos(playerYawRadians), dy = -Math.sin(playerYawRadians);

        double diff = (zIndex - alignZ) * MOUSE_DIVIDER;
        double diffX = dy * diff, diffY = -dx * diff;

        return new GridLine(
                centerX + diffX + dx * LINE_LENGTH, centerY + diffY + dy * LINE_LENGTH,
                -dx * 2*LINE_LENGTH, -dy * 2*LINE_LENGTH,
                color, name, arrowWidth
        );
    }

    private GridLine makeEastWestLine(int centerX, int centerY, int xIndex, int color, String name, float arrowWidth) {
        double alignX = this.xOffset.get();
        double dx = Math.cos(playerYawRadians), dy = -Math.sin(playerYawRadians);

        double diff = (alignX - xIndex) * MOUSE_DIVIDER;
        double diffX = dx * diff, diffY = dy * diff;

        return new GridLine(
                centerX + diffX - dy * LINE_LENGTH, centerY + diffY + dx * LINE_LENGTH,
                dy * 2*LINE_LENGTH, -dx * 2*LINE_LENGTH,
                color, name, arrowWidth
        );
    }

    @Override
    public boolean mousePressed(double mouseX, double mouseY, int mouseButton) {
        xInput.mousePressed(mouseX, mouseY, mouseButton);
        zInput.mousePressed(mouseX, mouseY, mouseButton);

        if(mouseButton == 0) {
            if(lockNorthCheckBox.mousePressed(mouseX, mouseY, mouseButton)) {
                lockNorth.set(lockNorthCheckBox.isChecked());
                this.updatePlayerYawRadians();
                return true;
            }
            if(mouseInAlignBox(mouseX, mouseY)) {
                aligningMode = true;
            }
        }

        return false;
    }

    private boolean mouseInAlignBox(double mouseX, double mouseY) {
        int width = this.getWidth();
        return mouseX >= 10 && mouseX <= width - 10 &&
                mouseY >= 20 + ALIGNBOX_MARGIN_TOP && mouseY <= 20 + ALIGNBOX_MARGIN_TOP + ALIGNBOX_HEIGHT;
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

    @RequiredArgsConstructor
    private static class GridLine {
        private final double x, y, dx, dy;
        private final int color;
        @Nullable
        private final String label;
        private final float arrowWidth;

        private Double intersectionParam;
        private HorizontalAlign labelHAlign;
        private VerticalAlign labelVAlign;

        @Nullable
        private Double getIntersectionParameter(GridLine other) {
            double denominator = dx * other.dy - dy * other.dx;
            if(denominator == 0) return null;

            double t = ((other.x - x) * other.dy - (other.y - y) * other.dx) / denominator;
            double u = ((other.x - x) * dy - (other.y - y) * dx) / denominator;
            if(t < 0 || 1 < t || u < 0 || 1 < u) return null;

            return t;
        }
    }
}
