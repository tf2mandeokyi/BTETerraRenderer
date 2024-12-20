package com.mndk.bteterrarenderer.core.gui.mapaligner;

import com.mndk.bteterrarenderer.BTETerraRenderer;
import com.mndk.bteterrarenderer.util.accessor.PropertyAccessor;
import com.mndk.bteterrarenderer.mcconnector.McConnector;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.DrawContextWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.shape.GraphicsQuad;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.vertex.PosXY;
import com.mndk.bteterrarenderer.mcconnector.client.gui.HorizontalAlign;
import com.mndk.bteterrarenderer.mcconnector.client.gui.VerticalAlign;
import com.mndk.bteterrarenderer.mcconnector.client.mcfx.McFXElement;
import com.mndk.bteterrarenderer.mcconnector.util.ResourceLocationWrapper;
import com.mndk.bteterrarenderer.util.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
class MapAlignerBox extends McFXElement {
    
    private static final int ALIGNBOX_ARROW_SIZE = 7;

    private static final float MOUSE_DIVIDER = 30;
    
    private static final int ALIGNBOX_BACKGROUND_COLOR = 0x90000000;
    private static final int AXIS_LINE_COLOR = 0xFFFFFFFF;
    private static final int PRIMARY_LINE_COLOR = 0xFF8D8D8D;
    private static final int SECONDARY_LINE_COLOR = 0xFF4D4D4D;
    private static final int LINE_LENGTH = 1000;
    
    private static final ResourceLocationWrapper<?> ALIGNMENT_MARKER = McConnector.client().newResourceLocation(
            BTETerraRenderer.MODID, "textures/ui/alignment_marker.png"
    );
    
    private final int height;
    private final PropertyAccessor<Double> xOffset, zOffset;
    private final Runnable onOffsetChange;

    @Setter
    private double playerYawRadians = 0;
    private boolean aligningMode = false;
    private final List<GridLine> gridLines = new ArrayList<>();

    @Override
    public int getPhysicalHeight() {
        return this.height;
    }

    @Override
    protected void init() {}

    @Override
    public void onWidthChange() {}

    @Override
    public void drawElement(DrawContextWrapper<?> drawContextWrapper) {
        int elementWidth = this.getWidth();
        int centerX = elementWidth / 2, centerY = this.height / 2;

        // Box background
        drawContextWrapper.fillRect(0, 0, elementWidth, this.height, ALIGNBOX_BACKGROUND_COLOR);

        // Grids
        this.updateGridLines();
        this.drawGridLines(drawContextWrapper, elementWidth);

        // Center marker
        drawContextWrapper.drawWholeCenteredImage(ALIGNMENT_MARKER, centerX, centerY, 4, 4);
        String x = StringUtil.formatDoubleNicely(xOffset.get(), 2);
        String z = StringUtil.formatDoubleNicely(zOffset.get(), 2);
        String text = String.format("§rX §f%s§r, Z §f%s", x, z);
        drawContextWrapper.drawTextWithShadow(getDefaultFont(), text, HorizontalAlign.LEFT, VerticalAlign.TOP,
                centerX + 3, centerY + 3, MapAligner.MARKER_COLOR);
    }

    private void updateGridLines() {
        int width = this.getWidth();

        this.gridLines.clear();

        double alignX = xOffset.get(), alignZ = zOffset.get();
        int xi = (int) alignX, zi = (int) alignZ;
        int lineCount = (int) Math.ceil((width + this.height) / MOUSE_DIVIDER / 2);
        int centerX = width / 2, centerY = this.height / 2;

        // Secondary lines
        for (int z = zi - lineCount; z <= zi + lineCount; z++) {
            if (z % 5 == 0) continue;
            this.gridLines.add(makeNorthSouthLine(centerX, centerY, z, SECONDARY_LINE_COLOR, null, 0));
        }
        for (int x = xi - lineCount; x <= xi + lineCount; x++) {
            if (x % 5 == 0) continue;
            this.gridLines.add(makeEastWestLine(centerX, centerY, x, SECONDARY_LINE_COLOR, null, 0));
        }

        // Primary lines
        for (int z = zi - lineCount; z <= zi + lineCount; z++) {
            if (z % 5 != 0 || z == 0) continue;
            this.gridLines.add(makeNorthSouthLine(centerX, centerY, z, PRIMARY_LINE_COLOR, String.format(" Z(%d) ", z), 0.3f));
        }
        for (int x = xi - lineCount; x <= xi + lineCount; x++) {
            if (x % 5 != 0 || x == 0) continue;
            this.gridLines.add(makeEastWestLine(centerX, centerY, x, PRIMARY_LINE_COLOR, String.format(" X(%d) ", x), 0.3f));
        }

        // Axis lines
        this.gridLines.add(makeNorthSouthLine(centerX, centerY, 0, AXIS_LINE_COLOR, " Z(0) ", 0.5f));
        this.gridLines.add(makeEastWestLine(centerX, centerY, 0, AXIS_LINE_COLOR, " X(0) ", 0.5f));

        // Calculate arrow position & label alignments
        GridLine topLine = new GridLine(0, 0, width, 0, 0, null, 0);
        GridLine leftLine = new GridLine(0, 0, 0, this.height, 0, null, 0);
        GridLine bottomLine = new GridLine(0, this.height, width, 0, 0, null, 0);
        GridLine rightLine = new GridLine(width, 0, 0, this.height, 0, null, 0);
        for (GridLine line : this.gridLines) {
            Double t, maxT = null;
            HorizontalAlign hAlign = null;
            VerticalAlign vAlign = null;

            if ((t = line.getIntersectionParameter(topLine   )) != null)
            { maxT = t; hAlign = HorizontalAlign.CENTER; vAlign = VerticalAlign.BOTTOM; }
            if ((t = line.getIntersectionParameter(leftLine  )) != null && (maxT == null || t > maxT))
            { maxT = t; hAlign = HorizontalAlign.RIGHT ; vAlign = VerticalAlign.MIDDLE; }
            if ((t = line.getIntersectionParameter(bottomLine)) != null && (maxT == null || t > maxT))
            { maxT = t; hAlign = HorizontalAlign.CENTER; vAlign = VerticalAlign.TOP   ; }
            if ((t = line.getIntersectionParameter(rightLine )) != null && (maxT == null || t > maxT))
            { maxT = t; hAlign = HorizontalAlign.LEFT  ; vAlign = VerticalAlign.MIDDLE; }

            line.intersectionParam = maxT;
            line.labelHAlign = hAlign;
            line.labelVAlign = vAlign;
        }
    }

    private void drawGridLines(DrawContextWrapper<?> drawContextWrapper, int boxWidth) {
        // Draw lines & arrows
        drawContextWrapper.glPushRelativeScissor(0, 0, boxWidth, this.height);
        for (GridLine line : this.gridLines) {
            // line
            GraphicsQuad<PosXY> quad = drawContextWrapper.makeLineDxDy(line.x, line.y, line.dx, line.dy, 1);
            if (quad == null) return;
            drawContextWrapper.fillQuad(quad, line.color, 0);

            if (line.intersectionParam == null) continue;
            double t = line.intersectionParam;
            float x = (float) (line.x + t * line.dx);
            float y = (float) (line.y + t * line.dy);

            // arrow
            if (line.arrowWidth == 0) continue;
            double distance = Math.sqrt(line.dx * line.dx + line.dy * line.dy);
            float dxNorm = (float) (line.dx / distance);
            float dyNorm = (float) (line.dy / distance);
            float arrowBackX = x - dxNorm * ALIGNBOX_ARROW_SIZE;
            float arrowBackY = y - dyNorm * ALIGNBOX_ARROW_SIZE;
            float sideDx = dyNorm * ALIGNBOX_ARROW_SIZE * line.arrowWidth;
            float sideDy = -dxNorm * ALIGNBOX_ARROW_SIZE * line.arrowWidth;
            drawContextWrapper.fillQuad(GraphicsQuad.newPosXY(
                    new PosXY(x, y),
                    new PosXY(arrowBackX + sideDx, arrowBackY + sideDy),
                    new PosXY(arrowBackX - sideDx, arrowBackY - sideDy),
                    new PosXY(x, y)
            ), line.color, 0);
        }
        drawContextWrapper.glPopRelativeScissor();

        // Draw labels
        for (GridLine line : this.gridLines) {
            if (line.intersectionParam == null) continue;
            if (line.label == null) continue;
            double t = line.intersectionParam;
            float x = (float) (line.x + t * line.dx);
            float y = (float) (line.y + t * line.dy);
            drawContextWrapper.drawTextWithShadow(getDefaultFont(), line.label, line.labelHAlign, line.labelVAlign, x, y, line.color);
        }
    }

    private GridLine makeNorthSouthLine(int centerX, int centerY, int zIndex, int color, String name, float arrowWidth) {
        double alignZ = zOffset.get();
        double dx = Math.cos(playerYawRadians), dy = -Math.sin(playerYawRadians);

        double diff = (zIndex - alignZ) * MOUSE_DIVIDER;
        double diffX = dy * diff, diffY = -dx * diff;

        return new GridLine(
                centerX + diffX + dx * LINE_LENGTH, centerY + diffY + dy * LINE_LENGTH,
                -dx * 2 * LINE_LENGTH, -dy * 2 * LINE_LENGTH,
                color, name, arrowWidth
        );
    }

    private GridLine makeEastWestLine(int centerX, int centerY, int xIndex, int color, String name, float arrowWidth) {
        double alignX = xOffset.get();
        double dx = Math.cos(playerYawRadians), dy = -Math.sin(playerYawRadians);

        double diff = (alignX - xIndex) * MOUSE_DIVIDER;
        double diffX = dx * diff, diffY = dy * diff;

        return new GridLine(
                centerX + diffX - dy * LINE_LENGTH, centerY + diffY + dx * LINE_LENGTH,
                dy * 2 * LINE_LENGTH, -dx * 2 * LINE_LENGTH,
                color, name, arrowWidth
        );
    }

    @Override
    public boolean mousePressed(double mouseX, double mouseY, int mouseButton) {
        if (mouseButton == 0 && mouseInAlignBox(mouseX, mouseY)) {
            aligningMode = true;
        }
        return false;
    }

    private boolean mouseInAlignBox(double mouseX, double mouseY) {
        return mouseX >= 0 && mouseX <= this.getWidth() && mouseY >= 0 && mouseY <= this.height;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double pMouseX, double pMouseY) {
        if (aligningMode) {
            double dmx = (mouseX - pMouseX) / MOUSE_DIVIDER, dmy = (mouseY - pMouseY) / MOUSE_DIVIDER;

            double dx = dmx * Math.cos(playerYawRadians) - dmy * Math.sin(playerYawRadians);
            double dz = dmx * Math.sin(playerYawRadians) + dmy * Math.cos(playerYawRadians);

            xOffset.set(dx + xOffset.get());
            zOffset.set(dz + zOffset.get());

            onOffsetChange.run();
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
        this.aligningMode = false;
        return true;
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
            if (denominator == 0) return null;

            double t = ((other.x - x) * other.dy - (other.y - y) * other.dx) / denominator;
            double u = ((other.x - x) * dy - (other.y - y) * dx) / denominator;
            if (t < 0 || 1 < t || u < 0 || 1 < u) return null;

            return t;
        }
    }
}
