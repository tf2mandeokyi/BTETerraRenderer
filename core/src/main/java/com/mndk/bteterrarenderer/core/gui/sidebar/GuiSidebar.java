package com.mndk.bteterrarenderer.core.gui.sidebar;

import com.mndk.bteterrarenderer.core.gui.mcfx.McFX;
import com.mndk.bteterrarenderer.core.gui.mcfx.McFXElement;
import com.mndk.bteterrarenderer.core.gui.mcfx.button.McFXButton;
import com.mndk.bteterrarenderer.core.gui.mcfx.list.McFXVerticalList;
import com.mndk.bteterrarenderer.core.util.BTRUtil;
import com.mndk.bteterrarenderer.core.util.accessor.PropertyAccessor;
import com.mndk.bteterrarenderer.mcconnector.gui.component.AbstractGuiScreenCopy;
import com.mndk.bteterrarenderer.mcconnector.input.InputKey;
import com.mndk.bteterrarenderer.mcconnector.wrapper.DrawContextWrapper;

import java.util.List;
import java.util.function.Supplier;

// TODO: Add tab key
public abstract class GuiSidebar extends AbstractGuiScreenCopy {

    private static final int SIDEBAR_BACKGROUND_COLOR = 0x5F000000;
    private static final int WIDTH_CHANGE_BAR_COLOR = 0xFFFFFFFF;
    private static final int WIDTH_CHANGE_BAR_COLOR_HOVERED = HOVERED_COLOR;
    private static final int WIDTH_CHANGE_BAR_SHADOW = 0xFF383838;
    private static final int WIDTH_CHANGE_BAR_SHADOW_HOVERED = 0xFF3f3f28;

    private final McFXVerticalList listComponent, innerListComponent;
    public final PropertyAccessor<SidebarSide> side;
    private final McFXButton sideChangingButton;
    private final boolean guiPausesGame;
    /** Only use this in {@link #initGui()} */
    private final int elementPaddingSide, paddingTopBottom, elementDistance;

    public final PropertyAccessor<Double> sidebarWidth;
    private double mouseClickX, initialWidth;
    private boolean widthChangingState, widthChangeBarHoverState;


    public GuiSidebar(int elementPaddingSide, int paddingTopBottom, int elementDistance, boolean guiPausesGame,
                      PropertyAccessor<Double> sidebarWidth,
                      PropertyAccessor<SidebarSide> side) {
        this.listComponent = McFX.vList(0, 0, true);
        this.sideChangingButton = McFX.button("", this::toggleSide);

        // Put these in constructor to prevent the vertical slider value not being reset
        Supplier<Integer> remainingHeightGetter = () -> this.getHeight() - this.sideChangingButton.getPhysicalHeight();
        this.innerListComponent = McFX.vList(0, 0, remainingHeightGetter, false);

        this.elementPaddingSide = elementPaddingSide;
        this.paddingTopBottom = paddingTopBottom;
        this.elementDistance = elementDistance;

        this.side = side;
        this.widthChangingState = false;
        this.guiPausesGame = guiPausesGame;
        this.sidebarWidth = sidebarWidth;
    }

    protected abstract List<McFXElement> getSidebarElements();

    @Override
    public void initGui() {
        this.innerListComponent.clear()
                .add(McFX.div(this.paddingTopBottom))
                .add(McFX.vList(this.elementDistance, this.elementPaddingSide)
                        .addAll(this.getSidebarElements()))
                .add(McFX.div(this.paddingTopBottom));

        this.listComponent.clear()
                .add(this.sideChangingButton)
                .add(innerListComponent);

        this.listComponent.init(this.sidebarWidth.get().intValue());
        this.sideChangingButton.setDisplayString(side.get() == SidebarSide.LEFT ? ">>" : "<<");
    }

    private void toggleSide(McFXButton button, int mouseButton) {
        side.set(side.get() == SidebarSide.LEFT ? SidebarSide.RIGHT : SidebarSide.LEFT);
        button.setDisplayString(side.get() == SidebarSide.LEFT ? ">>" : "<<");
    }

    @Override
    public void tick() {
        this.listComponent.tick();
    }

    @Override
    public boolean mouseHovered(double mouseX, double mouseY, float partialTicks, boolean mouseHidden) {
        // Sidebar elements
        int sidebarLeft = this.getSidebarXRange()[0];
        boolean result = this.listComponent.mouseHovered(mouseX - sidebarLeft, mouseY, partialTicks, mouseHidden);
        if(result) return true;

        // Width change bar
        return this.widthChangeBarHoverState = Math.abs(mouseX - this.getWidthChangeBarX()) <= 4;
    }

    @Override
    protected void drawScreen(DrawContextWrapper<?> drawContextWrapper) {
        this.drawSidebarBackground(drawContextWrapper);

        drawContextWrapper.pushMatrix();

        int sidebarLeft = this.getSidebarXRange()[0];
        drawContextWrapper.translate(sidebarLeft, 0, 0);
        this.listComponent.drawComponent(drawContextWrapper);

        drawContextWrapper.popMatrix();
    }


    private void drawSidebarBackground(DrawContextWrapper<?> drawContextWrapper) {
        int height = this.getHeight();

        // Background
        int[] range = this.getSidebarXRange();
        drawContextWrapper.fillRect(range[0], 0, range[1], height, SIDEBAR_BACKGROUND_COLOR);

        // Width change bar
        int widthChangeBarX = this.getWidthChangeBarX();
        int changeBarColor = this.widthChangeBarHoverState ? WIDTH_CHANGE_BAR_COLOR_HOVERED : WIDTH_CHANGE_BAR_COLOR;
        int changeBarShadow = this.widthChangeBarHoverState ? WIDTH_CHANGE_BAR_SHADOW_HOVERED : WIDTH_CHANGE_BAR_SHADOW;
        drawContextWrapper.fillRect(widthChangeBarX - 1, 0, widthChangeBarX, height,
                this.side.get() == SidebarSide.LEFT ? changeBarShadow : changeBarColor);
        drawContextWrapper.fillRect(widthChangeBarX, 0, widthChangeBarX + 1, height,
                this.side.get() == SidebarSide.RIGHT ? changeBarShadow : changeBarColor);
    }


    public boolean mousePressed(double mouseX, double mouseY, int mouseButton) {
        this.initialWidth = sidebarWidth.get();
        this.mouseClickX = mouseX;

        int sidebarLeft = this.getSidebarXRange()[0];
        boolean result = this.listComponent.mousePressed(mouseX - sidebarLeft, mouseY, mouseButton);
        if(result) return true;

        return this.widthChangingState = Math.abs(mouseX - this.getWidthChangeBarX()) <= 4;
    }


    public boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
        int sidebarLeft = this.getSidebarXRange()[0];
        this.listComponent.mouseReleased(mouseX - sidebarLeft, mouseY, mouseButton);
        this.widthChangingState = false;
        return true;
    }


    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollAmount) {
        int sidebarLeft = this.getSidebarXRange()[0];
        return this.listComponent.mouseScrolled(mouseX - sidebarLeft, mouseY, scrollAmount);
    }


    private int getSidebarWidth() {
        return sidebarWidth.get().intValue();
    }

    /**
     * @return [ leftX, rightX ]
     */
    private int[] getSidebarXRange() {
        int screenWidth = this.getWidth(), sidebarWidth = this.getSidebarWidth();
        return this.side.get() == SidebarSide.LEFT ?
                new int[] { 0, sidebarWidth } : new int[] { screenWidth - sidebarWidth, screenWidth };
    }

    private int getWidthChangeBarX() {
        int[] range = this.getSidebarXRange();
        return this.side.get() == SidebarSide.LEFT ? range[1] : range[0];
    }


    public boolean keyTyped(char key, int keyCode) {
        return this.listComponent.keyTyped(key, keyCode);
    }


    @Override
    public boolean keyPressed(InputKey key) {
        return this.listComponent.keyPressed(key);
    }


    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double pMouseX, double pMouseY) {
        if(this.widthChangingState) {
            double dMouseX = mouseX - mouseClickX;
            if(side.get() == SidebarSide.RIGHT) dMouseX = -dMouseX;
            double sidebarWidth = BTRUtil.clamp(initialWidth + dMouseX, 180, 320);

            this.sidebarWidth.set(sidebarWidth);
            this.listComponent.onWidthChange((int) sidebarWidth);
            return true;
        }

        int sidebarLeft = this.getSidebarXRange()[0];
        return this.listComponent.mouseDragged(
                mouseX - sidebarLeft, mouseY, mouseButton,
                pMouseX - sidebarLeft, pMouseY);
    }

    @Override
    public void onClose() {}

    @Override
    public boolean doesScreenPauseGame() {
        return guiPausesGame;
    }
}
