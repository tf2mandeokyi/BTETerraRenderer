package com.mndk.bteterrarenderer.gui.sidebar.mapaligner;

import com.mndk.bteterrarenderer.BTETerraRendererConstants;
import com.mndk.bteterrarenderer.connector.DependencyConnectorSupplier;
import com.mndk.bteterrarenderer.connector.graphics.GraphicsConnector;
import com.mndk.bteterrarenderer.connector.gui.GuiStaticConnector;
import com.mndk.bteterrarenderer.connector.minecraft.I18nConnector;
import com.mndk.bteterrarenderer.connector.minecraft.IResourceLocation;
import com.mndk.bteterrarenderer.connector.minecraft.InputKey;
import com.mndk.bteterrarenderer.connector.minecraft.MinecraftClientConnector;
import com.mndk.bteterrarenderer.gui.components.GuiCheckBoxImpl;
import com.mndk.bteterrarenderer.gui.components.GuiNumberInput;
import com.mndk.bteterrarenderer.gui.sidebar.GuiSidebarElement;
import com.mndk.bteterrarenderer.util.GetterSetter;

public class SidebarMapAligner extends GuiSidebarElement {

    private static final int ALIGNBOX_MARGIN_VERT = 10;
    private static final int ALIGNBOX_MARGIN_SIDE = 15;
    private static final int ALIGNBOX_HEIGHT = 150;

    private static final double MOUSE_DIVIDER = 30;

    private static final IResourceLocation ALIGNMENT_MARKER = DependencyConnectorSupplier.INSTANCE.newResourceLocation(
            BTETerraRendererConstants.MODID, "textures/ui/alignment_marker.png"
    );

    private GuiNumberInput xInput, zInput;
    private final GetterSetter<Double> xOffset, zOffset;

    private GuiCheckBoxImpl lockNorthCheckBox;
    private final GetterSetter<Boolean> lockNorth;

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
        int width = parent.elementWidth.get().intValue();
        this.xInput = new GuiNumberInput(
                0, 0, width / 2 - 3, 20,
                xOffset, "X = "
        );
        this.zInput = new GuiNumberInput(
                width / 2 + 3, 0, width / 2 - 3, 20,
                zOffset, "Z = "
        );
        this.lockNorthCheckBox = new GuiCheckBoxImpl(
                ALIGNBOX_MARGIN_SIDE, 20 + ALIGNBOX_MARGIN_VERT * 2 + ALIGNBOX_HEIGHT,
                I18nConnector.INSTANCE.format("gui.bteterrarenderer.new_settings.lock_north"), this.lockNorth.get()
        );
        this.setPlayerYawRadians();
    }

    private void setPlayerYawRadians() {
        this.playerYawRadians = lockNorthCheckBox.isChecked() ?
                Math.PI :
                Math.toRadians(MinecraftClientConnector.INSTANCE.getPlayerRotationYaw());
    }

    @Override
    public int getHeight() {
        return 40 + ALIGNBOX_MARGIN_VERT * 2 + ALIGNBOX_HEIGHT;
    }

    @Override
    public void onWidthChange(double newWidth) {
        this.xInput.setWidth((int) (newWidth / 2 - 3));
        this.zInput.setWidth((int) (newWidth / 2 - 3));
        this.zInput.setX((int) (newWidth / 2 + 3));
    }

    @Override
    public void drawComponent(Object poseStack, double mouseX, double mouseY, float partialTicks) {
        xInput.drawComponent(poseStack, mouseX, mouseY, partialTicks);
        zInput.drawComponent(poseStack, mouseX, mouseY, partialTicks);
        lockNorthCheckBox.drawComponent(poseStack, mouseX, mouseY, partialTicks);
        this.drawAlignBox(poseStack);
    }

    private void drawAlignBox(Object poseStack) {
        int elementWidth = parent.elementWidth.get().intValue(), boxWidth = elementWidth - ALIGNBOX_MARGIN_SIDE * 2;
        double alignX = this.xOffset.get(), alignZ = this.zOffset.get();
        int xi = (int) alignX, zi = (int) alignZ;
        int lineCount = (int) Math.ceil((boxWidth + ALIGNBOX_HEIGHT) / MOUSE_DIVIDER / 2);
        double dx = Math.cos(playerYawRadians), dy = -Math.sin(playerYawRadians);
        int centerX = elementWidth / 2, centerY = 20 + ALIGNBOX_MARGIN_VERT + ALIGNBOX_HEIGHT / 2;

        // Enable box clipping
        GraphicsConnector.INSTANCE.glEnableScissorTest();
        GraphicsConnector.INSTANCE.glRelativeScissor(poseStack,
                ALIGNBOX_MARGIN_SIDE, 20 + ALIGNBOX_MARGIN_VERT,
                elementWidth - 2 * ALIGNBOX_MARGIN_SIDE, ALIGNBOX_HEIGHT
        );

        GuiStaticConnector.INSTANCE.fillRect(poseStack,
                ALIGNBOX_MARGIN_SIDE, 20 + ALIGNBOX_MARGIN_VERT,
                elementWidth - ALIGNBOX_MARGIN_SIDE, 20 + ALIGNBOX_MARGIN_VERT + ALIGNBOX_HEIGHT,
                0xBB000000
        );

        // North-South lines
        for(int z = zi - lineCount; z <= zi + lineCount; z++) {
            double diff = (z - alignZ) * MOUSE_DIVIDER;
            double diffX = dy * diff, diffY = -dx * diff;
            int color = z == 0 ? 0xFFFF0000 : (z % 5 == 0 ? 0xFFFFFFFF : 0xFF8E8E8E);

            GuiStaticConnector.INSTANCE.drawLineDxDy(poseStack,
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

            GuiStaticConnector.INSTANCE.drawLineDxDy(poseStack,
                    centerX + diffX - dy * 1000, centerY + diffY + dx * 1000,
                    dy * 2000, -dx * 2000,
                    1, color
            );
        }

        // Disable box clipping
        GraphicsConnector.INSTANCE.glDisableScissorTest();

        // Center marker
        GuiStaticConnector.INSTANCE.drawWholeCenteredImage(poseStack, ALIGNMENT_MARKER, centerX, centerY, 4, 4);
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
            if(isMouseInAlignBox(mouseX, mouseY)) {
                aligningMode = true;
            }
        }

        return false;
    }

    private boolean isMouseInAlignBox(double mouseX, double mouseY) {
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
