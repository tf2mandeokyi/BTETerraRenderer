package com.mndk.bteterrarenderer.core.gui.sidebar;

import com.mndk.bteterrarenderer.mcconnector.McConnector;
import com.mndk.bteterrarenderer.mcconnector.client.gui.screen.NativeGuiScreenWrappedScreen;
import com.mndk.bteterrarenderer.mcconnector.client.gui.screen.NativeGuiScreenWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.input.InputKey;
import com.mndk.bteterrarenderer.mcconnector.client.mcfx.McFX;
import com.mndk.bteterrarenderer.mcconnector.client.mcfx.McFXElement;
import com.mndk.bteterrarenderer.mcconnector.client.mcfx.McFXScreen;
import com.mndk.bteterrarenderer.mcconnector.client.mcfx.button.McFXButton;
import com.mndk.bteterrarenderer.mcconnector.client.mcfx.list.McFXHorizontalList;
import com.mndk.bteterrarenderer.mcconnector.client.mcfx.list.McFXVerticalList;
import com.mndk.bteterrarenderer.mcconnector.client.mcfx.list.WidthFunction;
import com.mndk.bteterrarenderer.mcconnector.client.mcfx.wrapper.McFXScreenWrapper;
import com.mndk.bteterrarenderer.util.BTRUtil;
import com.mndk.bteterrarenderer.util.accessor.PropertyAccessor;

import java.util.List;
import java.util.function.Supplier;

// TODO: Add tab key
public abstract class GuiSidebar extends McFXScreen<McFXHorizontalList> {

    private static final int SIDEBAR_BACKGROUND_COLOR = 0x5F000000;
    private static final int WIDTH_CHANGE_BAR_COLOR = 0xFFFFFFFF;
    private static final int WIDTH_CHANGE_BAR_COLOR_HOVERED = HOVERED_COLOR;
    private static final int WIDTH_CHANGE_BAR_SHADOW = 0xFF383838;
    private static final int WIDTH_CHANGE_BAR_SHADOW_HOVERED = 0xFF3f3f28;

    private final McFXVerticalList listComponent;
    private final McFXScreenWrapper chatScreenWrapper;
    private final McFXButton sideChangingButton;
    private final McFXElement widthChangeBar, widthChangeBarShadow;

    public final PropertyAccessor<SidebarSide> side;
    /** Only use this in {@link #initGui()} */
    private final int elementPaddingSide, paddingTopBottom, elementDistance;

    public final PropertyAccessor<Double> sidebarWidth;
    private double mouseClickX, initialWidth;
    private boolean widthChangingState;
    private boolean isChatFocused;


    public GuiSidebar(int elementPaddingSide, int paddingTopBottom, int elementDistance, boolean guiPausesGame,
                      PropertyAccessor<Double> sidebarWidth,
                      PropertyAccessor<SidebarSide> side) {
        super(McFX.hList(0, true), guiPausesGame, false);
        this.sideChangingButton = McFX.button("", this::toggleSide);
        this.chatScreenWrapper = McFX.screenWrapper(this::getHeight);
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
        this.sidebarWidth = sidebarWidth;
    }

    protected abstract List<McFXElement> getSidebarElements();

    @Override
    protected void initGui() {
        super.initGui();

        this.listComponent.clear()
                .add(McFX.div(this.paddingTopBottom))
                .add(McFX.vList(this.elementDistance, this.elementPaddingSide)
                        .addAll(this.getSidebarElements()))
                .add(McFX.div(this.paddingTopBottom));

        this.updateSide();
    }

    private void toggleSide(McFXButton button, int mouseButton) {
        side.set(side.get() == SidebarSide.LEFT ? SidebarSide.RIGHT : SidebarSide.LEFT);
        this.updateSide();
    }

    private void updateSide() {
        if (side.get() == SidebarSide.LEFT) {
            McFXVerticalList sidebar = McFX.vList(0, 0)
                    .add(this.sideChangingButton)
                    .add(McFX.hList()
                            .add(this.listComponent, null)
                            .add(this.widthChangeBarShadow, WidthFunction.px(1)));

            this.getMainComponent().clear()
                    .add(sidebar, WidthFunction.px(this.sidebarWidth))
                    .add(this.widthChangeBar, WidthFunction.px(1))
                    .add(this.chatScreenWrapper, null);
        }
        else {
            McFXVerticalList sidebar = McFX.vList(0, 0)
                    .add(this.sideChangingButton)
                    .add(McFX.hList()
                            .add(this.widthChangeBarShadow, WidthFunction.px(1))
                            .add(this.listComponent, null));

            this.getMainComponent().clear()
                    .add(this.chatScreenWrapper, null)
                    .add(this.widthChangeBar, WidthFunction.px(1))
                    .add(sidebar, WidthFunction.px(this.sidebarWidth));
        }
        this.sideChangingButton.setDisplayString(side.get() == SidebarSide.LEFT ? ">>" : "<<");
    }

    @Override
    public boolean mouseHovered(int mouseX, int mouseY, float partialTicks) {
        this.isChatFocused = !this.chatScreenWrapper.isEmpty();

        if (super.mouseHovered(mouseX, mouseY, partialTicks)) return true;

        // Width change bar
        boolean widthChangeBarHoverState = Math.abs(mouseX - this.getWidthChangeBarX()) <= 4;
        if (widthChangeBarHoverState) {
            this.widthChangeBar.setBackgroundColor(WIDTH_CHANGE_BAR_COLOR_HOVERED);
            this.widthChangeBarShadow.setBackgroundColor(WIDTH_CHANGE_BAR_SHADOW_HOVERED);
        } else {
            this.widthChangeBar.setBackgroundColor(WIDTH_CHANGE_BAR_COLOR);
            this.widthChangeBarShadow.setBackgroundColor(WIDTH_CHANGE_BAR_SHADOW);
        }
        return widthChangeBarHoverState;
    }

    @Override
    public boolean mousePressed(double mouseX, double mouseY, int mouseButton) {
        this.initialWidth = sidebarWidth.get();
        this.mouseClickX = mouseX;

        if (super.mousePressed(mouseX, mouseY, mouseButton)) return true;
        return this.widthChangingState = Math.abs(mouseX - this.getWidthChangeBarX()) <= 4;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
        super.mouseReleased(mouseX, mouseY, mouseButton);
        this.widthChangingState = false;
        return true;
    }

    private int getWidthChangeBarX() {
        int screenWidth = this.getWidth(), sidebarWidth = this.sidebarWidth.get().intValue();
        return this.side.get() == SidebarSide.LEFT ? sidebarWidth : screenWidth - sidebarWidth;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double pMouseX, double pMouseY) {
        if (this.widthChangingState) {
            double dMouseX = mouseX - mouseClickX;
            if (side.get() == SidebarSide.RIGHT) dMouseX = -dMouseX;

            double sidebarWidth = BTRUtil.clamp(initialWidth + dMouseX, 180, 320);
            this.sidebarWidth.set(sidebarWidth);
            this.getMainComponent().onWidthChange(); // Not running this will cause a visual lag
            return true;
        }

        return super.mouseDragged(mouseX, mouseY, mouseButton, pMouseX, pMouseY);
    }

    @Override
    public boolean keyPressed(InputKey key, int scanCode, int modifiers) {
        if (this.chatScreenWrapper.isEmpty()) {
            if (key == InputKey.KEY_ESCAPE) {
                McConnector.client().displayGuiScreen(null);
                return true;
            }
            // TODO: Use user-bound key instead of fixed key like this
            else if (key == InputKey.KEY_T) {
                NativeGuiScreenWrapper nativeScreen = McConnector.client().newChatScreen("");
                NativeGuiScreenWrappedScreen screen = new NativeGuiScreenWrappedScreen(nativeScreen, true);
                this.chatScreenWrapper.setScreen(screen);
                // no return statement here, or else a letter "t" will be left when the chat is initialized.
            }
            else if (key == InputKey.KEY_SLASH) {
                NativeGuiScreenWrapper nativeScreen = McConnector.client().newChatScreen("/");
                NativeGuiScreenWrappedScreen screen = new NativeGuiScreenWrappedScreen(nativeScreen, true);
                this.chatScreenWrapper.setScreen(screen);
                // no return statement here, or else a letter "/" will be left when the chat is initialized.
            }
        }

        return super.keyPressed(key, scanCode, modifiers);
    }

    @Override
    public boolean isChatFocused() {
        return this.isChatFocused;
    }

    @Override
    public void onRemoved() {
        this.isChatFocused = false;
    }
}
