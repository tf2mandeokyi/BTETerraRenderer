package com.mndk.bteterrarenderer.core.gui.sidebar;

import com.mndk.bteterrarenderer.core.gui.mcfx.McFX;
import com.mndk.bteterrarenderer.core.gui.mcfx.McFXElement;
import com.mndk.bteterrarenderer.core.gui.mcfx.button.McFXButton;
import com.mndk.bteterrarenderer.core.gui.mcfx.list.McFXHorizontalList;
import com.mndk.bteterrarenderer.core.gui.mcfx.list.McFXVerticalList;
import com.mndk.bteterrarenderer.core.gui.mcfx.list.WidthFunction;
import com.mndk.bteterrarenderer.core.gui.mcfx.wrapper.McFXWrapper;
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

    private final McFXHorizontalList mainComponent;
    private final McFXVerticalList listComponent;
    private final McFXWrapper chatScreenWrapper;
    private final McFXButton sideChangingButton;
    private final McFXElement widthChangeBar, widthChangeBarShadow;

    public final PropertyAccessor<SidebarSide> side;
    private final boolean guiPausesGame;
    /** Only use this in {@link #initGui()} */
    private final int elementPaddingSide, paddingTopBottom, elementDistance;

    public final PropertyAccessor<Double> sidebarWidth;
    private double mouseClickX, initialWidth;
    private int prevScreenWidth, prevScreenHeight;
    private boolean widthChangingState;


    public GuiSidebar(int elementPaddingSide, int paddingTopBottom, int elementDistance, boolean guiPausesGame,
                      PropertyAccessor<Double> sidebarWidth,
                      PropertyAccessor<SidebarSide> side) {
        this.mainComponent = McFX.hList(0, true);
        this.sideChangingButton = McFX.button("", this::toggleSide);
        this.chatScreenWrapper = McFX.wrapper();
        this.widthChangeBar = McFX.div(this::getHeight).setBackgroundColor(WIDTH_CHANGE_BAR_COLOR);

        // Put these in constructor to prevent the vertical slider value not being reset
        Supplier<Integer> remainingHeightGetter = () -> this.getHeight() - this.sideChangingButton.getPhysicalHeight();
        this.listComponent = McFX.vList(0, 0, remainingHeightGetter, false);
        this.listComponent.setBackgroundColor(SIDEBAR_BACKGROUND_COLOR);
        this.widthChangeBarShadow = McFX.div(remainingHeightGetter).setBackgroundColor(WIDTH_CHANGE_BAR_SHADOW);

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
        this.listComponent.clear()
                .add(McFX.div(this.paddingTopBottom))
                .add(McFX.vList(this.elementDistance, this.elementPaddingSide)
                        .addAll(this.getSidebarElements()))
                .add(McFX.div(this.paddingTopBottom));

        this.mainComponent.init(this.getWidth());
        this.updateSide();
    }

    private void toggleSide(McFXButton button, int mouseButton) {
        side.set(side.get() == SidebarSide.LEFT ? SidebarSide.RIGHT : SidebarSide.LEFT);
        this.updateSide();
    }

    private void updateSide() {
        if(side.get() == SidebarSide.LEFT) {
            McFXVerticalList sidebar = McFX.vList(0, 0)
                    .add(this.sideChangingButton)
                    .add(McFX.hList()
                            .add(this.listComponent, null)
                            .add(this.widthChangeBarShadow, WidthFunction.px(1))
                    );

            this.mainComponent.clear()
                    .add(sidebar, WidthFunction.px(this.sidebarWidth))
                    .add(this.widthChangeBar, WidthFunction.px(1))
                    .add(this.chatScreenWrapper, null);
        }
        else {
            McFXVerticalList sidebar = McFX.vList(0, 0)
                    .add(this.sideChangingButton)
                    .add(McFX.hList()
                            .add(this.widthChangeBarShadow, WidthFunction.px(1))
                            .add(this.listComponent, null)
                    );

            this.mainComponent.clear()
                    .add(this.chatScreenWrapper, null)
                    .add(this.widthChangeBar, WidthFunction.px(1))
                    .add(sidebar, WidthFunction.px(this.sidebarWidth));
        }
        this.sideChangingButton.setDisplayString(side.get() == SidebarSide.LEFT ? ">>" : "<<");
    }

    @Override
    public void tick() {
        if(prevScreenWidth != this.getWidth() || prevScreenHeight != this.getHeight()) {
            this.mainComponent.onWidthChange(this.getWidth());
            this.prevScreenWidth = this.getWidth();
            this.prevScreenHeight = this.getHeight();
        }
        this.mainComponent.tick();
    }

    @Override
    public boolean mouseHovered(double mouseX, double mouseY, float partialTicks, boolean mouseHidden) {
        // Sidebar elements
        boolean result = this.mainComponent.mouseHovered(mouseX, mouseY, partialTicks, mouseHidden);
        if(result) return true;

        // Width change bar
        boolean widthChangeBarHoverState = Math.abs(mouseX - this.getWidthChangeBarX()) <= 4;
        if(widthChangeBarHoverState) {
            this.widthChangeBar.setBackgroundColor(WIDTH_CHANGE_BAR_COLOR_HOVERED);
            this.widthChangeBarShadow.setBackgroundColor(WIDTH_CHANGE_BAR_SHADOW_HOVERED);
        } else {
            this.widthChangeBar.setBackgroundColor(WIDTH_CHANGE_BAR_COLOR);
            this.widthChangeBarShadow.setBackgroundColor(WIDTH_CHANGE_BAR_SHADOW);
        }
        return widthChangeBarHoverState;
    }

    @Override
    protected void drawScreen(DrawContextWrapper<?> drawContextWrapper) {
        drawContextWrapper.pushMatrix();
        this.mainComponent.drawComponent(drawContextWrapper);
        drawContextWrapper.popMatrix();
    }


    public boolean mousePressed(double mouseX, double mouseY, int mouseButton) {
        this.initialWidth = sidebarWidth.get();
        this.mouseClickX = mouseX;

        boolean result = this.mainComponent.mousePressed(mouseX, mouseY, mouseButton);
        if(result) return true;

        return this.widthChangingState = Math.abs(mouseX - this.getWidthChangeBarX()) <= 4;
    }


    public boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
        this.mainComponent.mouseReleased(mouseX, mouseY, mouseButton);
        this.widthChangingState = false;
        return true;
    }


    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollAmount) {
        return this.mainComponent.mouseScrolled(mouseX, mouseY, scrollAmount);
    }


    private int getSidebarWidth() {
        return sidebarWidth.get().intValue();
    }


    private int getWidthChangeBarX() {
        int screenWidth = this.getWidth(), sidebarWidth = this.getSidebarWidth();
        return this.side.get() == SidebarSide.LEFT ? sidebarWidth : screenWidth - sidebarWidth;
    }


    public boolean keyTyped(char key, int keyCode) {
        return this.mainComponent.keyTyped(key, keyCode);
    }


    @Override
    public boolean keyPressed(InputKey key) {
        return this.mainComponent.keyPressed(key);
    }


    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double pMouseX, double pMouseY) {
        if(this.widthChangingState) {
            double dMouseX = mouseX - mouseClickX;
            if(side.get() == SidebarSide.RIGHT) dMouseX = -dMouseX;

            double sidebarWidth = BTRUtil.clamp(initialWidth + dMouseX, 180, 320);
            this.sidebarWidth.set(sidebarWidth);
            this.mainComponent.onWidthChange(); // Not running this will cause a visual lag
            return true;
        }

        return this.mainComponent.mouseDragged(mouseX, mouseY, mouseButton, pMouseX, pMouseY);
    }

    @Override
    public void onClose() {}

    @Override
    public boolean doesScreenPauseGame() {
        return guiPausesGame;
    }
}
