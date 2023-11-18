package com.mndk.bteterrarenderer.core.gui.sidebar;

import com.mndk.bteterrarenderer.core.graphics.GlGraphicsManager;
import com.mndk.bteterrarenderer.core.gui.RawGuiManager;
import com.mndk.bteterrarenderer.core.gui.components.AbstractGuiScreenCopy;
import com.mndk.bteterrarenderer.core.gui.sidebar.button.SidebarButton;
import com.mndk.bteterrarenderer.core.gui.sidebar.decorator.SidebarBlank;
import com.mndk.bteterrarenderer.core.gui.sidebar.wrapper.SidebarElementListComponent;
import com.mndk.bteterrarenderer.core.input.InputKey;
import com.mndk.bteterrarenderer.core.util.BTRUtil;
import com.mndk.bteterrarenderer.core.util.accessor.PropertyAccessor;
import lombok.Setter;

import java.util.List;

// TODO: Add tab key
public abstract class GuiSidebar extends AbstractGuiScreenCopy {

    private static final int SIDEBAR_BACKGROUND_COLOR = 0x5F000000;
    private static final int WIDTH_CHANGE_BAR_COLOR = 0xFFFFFFFF;
    private static final int WIDTH_CHANGE_BAR_COLOR_HOVERED = HOVERED_COLOR;
    private static final int WIDTH_CHANGE_BAR_SHADOW = 0xFF383838;
    private static final int WIDTH_CHANGE_BAR_SHADOW_HOVERED = 0xFF3f3f28;

    private final SidebarElementListComponent listComponent;
    @Setter private PropertyAccessor<SidebarSide> side;
    private final boolean guiPausesGame;

    public final PropertyAccessor<Double> sidebarWidth;
    private double mouseClickX, initialWidth;
    private boolean widthChangingState, widthChangeBarHoverState;

    private final SidebarGuiChat guiChat;


    public GuiSidebar(int elementPaddingSide, int paddingTopBottom, int elementDistance, boolean guiPausesGame,
                      PropertyAccessor<Double> sidebarWidth,
                      PropertyAccessor<SidebarSide> side) {
        this.listComponent = new SidebarElementListComponent(0, 0, null, true);

        // Side changing button
        SidebarButton button = new SidebarButton(side.get() == SidebarSide.LEFT ? ">>" : "<<", (self, mouseButton) -> {
            side.set(side.get() == SidebarSide.LEFT ? SidebarSide.RIGHT : SidebarSide.LEFT);
            self.setDisplayString(side.get() == SidebarSide.LEFT ? ">>" : "<<");
        });
        this.listComponent.add(button);

        // Sidebar element list
        SidebarBlank blank = new SidebarBlank(paddingTopBottom);
        SidebarElementListComponent elementList = new SidebarElementListComponent(
                elementDistance, elementPaddingSide, () -> this.getHeight() - button.getPhysicalHeight(), false);
        elementList.add(blank);
        elementList.addAll(this.getElements());
        elementList.add(blank);
        this.listComponent.add(elementList);

        this.side = side;
        this.widthChangingState = false;
        this.guiPausesGame = guiPausesGame;
        this.sidebarWidth = sidebarWidth;

        this.guiChat = new SidebarGuiChat();
    }

    protected abstract List<GuiSidebarElement> getElements();

    @Override
    public void initGui() {
        this.guiChat.initGui();
        this.guiChat.changeSideMargin(side.get(), this.sidebarWidth.get().intValue());
        this.listComponent.init(this.sidebarWidth.get().intValue());
    }

    @Override
    public void tick() {
        this.listComponent.tick();
        if(this.guiChat.isOpened()) {
            this.guiChat.updateScreen();
        }
    }

    @Override
    public boolean mouseHovered(double mouseX, double mouseY, float partialTicks, boolean mouseHidden) {
        if(this.guiChat.isOpened()) {
            this.guiChat.mouseHovered(mouseX, mouseY, partialTicks);
        }

        // Sidebar elements
        int sidebarLeft = this.getSidebarXRange()[0];
        boolean result = this.listComponent.mouseHovered(mouseX - sidebarLeft, mouseY, partialTicks, mouseHidden);
        if(result) return true;

        // Width change bar
        return this.widthChangeBarHoverState = Math.abs(mouseX - this.getWidthChangeBarX()) <= 4;
    }

    @Override
    protected void drawScreen(Object poseStack) {
        if(this.guiChat.isOpened()) {
            this.guiChat.drawScreen(poseStack);
        }

        this.drawSidebarBackground(poseStack);

        GlGraphicsManager.glPushMatrix(poseStack);

        int sidebarLeft = this.getSidebarXRange()[0];
        GlGraphicsManager.glTranslate(poseStack, sidebarLeft, 0, 0);
        this.listComponent.drawComponent(poseStack);

        GlGraphicsManager.glPopMatrix(poseStack);
    }


    private void drawSidebarBackground(Object poseStack) {
        int height = this.getHeight();

        // Background
        int[] range = this.getSidebarXRange();
        RawGuiManager.fillRect(poseStack, range[0], 0, range[1], height, SIDEBAR_BACKGROUND_COLOR);

        // Width change bar
        int widthChangeBarX = this.getWidthChangeBarX();
        int changeBarColor = this.widthChangeBarHoverState ? WIDTH_CHANGE_BAR_COLOR_HOVERED : WIDTH_CHANGE_BAR_COLOR;
        int changeBarShadow = this.widthChangeBarHoverState ? WIDTH_CHANGE_BAR_SHADOW_HOVERED : WIDTH_CHANGE_BAR_SHADOW;
        RawGuiManager.fillRect(poseStack, widthChangeBarX - 1, 0, widthChangeBarX, height,
                this.side.get() == SidebarSide.LEFT ? changeBarShadow : changeBarColor);
        RawGuiManager.fillRect(poseStack, widthChangeBarX, 0, widthChangeBarX + 1, height,
                this.side.get() == SidebarSide.RIGHT ? changeBarShadow : changeBarColor);
    }


    public boolean mousePressed(double mouseX, double mouseY, int mouseButton) {
        if(this.guiChat.isOpened() && this.guiChat.mouseClickResponse(mouseX, mouseY, mouseButton))
            return true;

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
        if(this.guiChat.isOpened() && !this.mouseOnSidebar(mouseX)) {
            // TODO: Maybe I should consider removing guiChat from sidebar
            this.guiChat.handleMouseInput();
        }

        int sidebarLeft = this.getSidebarXRange()[0];
        return this.listComponent.mouseScrolled(mouseX - sidebarLeft, mouseY, scrollAmount);
    }


    private boolean mouseOnSidebar(double mouseX) {
        int[] range = this.getSidebarXRange();
        return mouseX >= range[0] && mouseX <= range[1];
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
        if(this.guiChat.isOpened() && this.guiChat.keyTypedResponse(key, keyCode)) {
            return true;
        }

        boolean keyTyped = this.listComponent.keyTyped(key, keyCode);
        if (keyTyped) return true;

        if(this.guiChat.isAvailable()) {
            // TODO: Maybe delete this
//            if(MinecraftClientManager.matchesChatOpenKeyCode(keyCode)) {
//                this.guiChat.setText("", true);
//                return true;
//            }
//            else if(MinecraftClientManager.matchesCommandOpenKeyCode(keyCode)) {
//                this.guiChat.setText("/", true);
//                return true;
//            }
        }
        return false;
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
            double sidebarWidth = BTRUtil.clamp(initialWidth + dMouseX, 170, 310);

            this.sidebarWidth.set(sidebarWidth);
            this.listComponent.onWidthChange((int) sidebarWidth);
            this.guiChat.changeSideMargin(side.get(), (int) sidebarWidth);
            return true;
        }

        int sidebarLeft = this.getSidebarXRange()[0];
        return this.listComponent.mouseDragged(
                mouseX - sidebarLeft, mouseY, mouseButton,
                pMouseX - sidebarLeft, pMouseY);
    }

    @Override
    public void onClose() {
        this.guiChat.setOpened(false);
    }

    @Override
    public boolean doesScreenPauseGame() {
        return guiPausesGame;
    }
}
