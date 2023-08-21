package com.mndk.bteterrarenderer.core.gui.sidebar;

import com.mndk.bteterrarenderer.core.graphics.GlGraphicsManager;
import com.mndk.bteterrarenderer.core.util.mixin.delegate.IScaledScreenSize;
import com.mndk.bteterrarenderer.core.gui.RawGuiManager;
import com.mndk.bteterrarenderer.core.util.input.InputKey;
import com.mndk.bteterrarenderer.core.util.minecraft.MinecraftClientManager;
import com.mndk.bteterrarenderer.core.gui.components.AbstractGuiScreenCopy;
import com.mndk.bteterrarenderer.core.util.BtrUtil;
import com.mndk.bteterrarenderer.core.util.accessor.PropertyAccessor;

// TODO: Add tab key
public class GuiSidebar extends AbstractGuiScreenCopy {

    private static final int SIDEBAR_BACKGROUND_COLOR = 0x5F000000;
    private static final int WIDTH_CHANGE_BAR_COLOR = 0xFFFFFFFF;
    private static final int WIDTH_CHANGE_BAR_COLOR_HOVERED = HOVERED_COLOR;
    private static final int WIDTH_CHANGE_BAR_SHADOW = 0xFF383838;
    private static final int WIDTH_CHANGE_BAR_SHADOW_HOVERED = 0xFF3f3f28;
    private static final int VERTICAL_SLIDER_WIDTH = 6;
    private static final int VERTICAL_SLIDER_PADDING = 2;
    private static final int VERTICAL_SLIDER_COLOR = NORMAL_TEXT_COLOR;
    private static final int VERTICAL_SLIDER_COLOR_HOVERED = HOVERED_COLOR;
    private static final int VERTICAL_SLIDER_COLOR_CLICKED = FOCUSED_BORDER_COLOR;

    protected final SidebarElementListComponent elementsComponent;
    private SidebarSide side;
    private final int elementPaddingSide;
    private final int paddingTopBottom;
    private final boolean guiPausesGame;
    private int verticalSliderValue, initialVerticalSliderValue;
    private boolean verticalSliderHoverState, verticalSliderChangingState;

    public final PropertyAccessor<Double> elementWidth;
    private double mouseClickX, mouseClickY, initialElementWidth;
    private boolean widthChangingState, widthChangeBarHoverState;

    private final SidebarGuiChat guiChat;


    public GuiSidebar(
            SidebarSide side, int elementPaddingSide, int paddingTopBottom, int elementDistance,
            PropertyAccessor<Double> elementWidth, boolean guiPausesGame
    ) {
        this.elementsComponent = new SidebarElementListComponent(elementDistance);
        this.side = side;
        this.elementPaddingSide = elementPaddingSide;
        this.paddingTopBottom = paddingTopBottom;
        this.verticalSliderValue = 0;
        this.widthChangingState = false;
        this.guiPausesGame = guiPausesGame;
        this.guiChat = new SidebarGuiChat();

        this.elementWidth = elementWidth;
    }


    public void setSide(SidebarSide side) {
        this.side = side;
    }

    @Override
    public void initGui() {
        this.guiChat.initGui(screenSize.get());
        this.guiChat.changeSideMargin(side, (int) (this.elementWidth.get() + 2 * this.elementPaddingSide));
        this.elementsComponent.initComponent(this);
    }

    @Override
    public void tick() {
        this.elementsComponent.tick();
        if(this.guiChat.isOpened()) {
            this.guiChat.updateScreen();
        }
    }

    @Override
    public boolean mouseHovered(double mouseX, double mouseY, float partialTicks, boolean mouseHidden) {
        if(this.guiChat.isOpened()) {
            this.guiChat.mouseHovered(mouseX, mouseY, partialTicks);
        }
        this.validateSliderValue();

        // Vertical slider
        boolean result = this.verticalSliderHoverState = this.mouseOnScrollBar(mouseX, mouseY);
        if(result) {
            this.widthChangeBarHoverState = false;
            return true;
        }

        // Sidebar elements
        int translateX = this.getElementsTranslateX();
        result = this.elementsComponent.mouseHovered(mouseX - translateX,
                mouseY - this.paddingTopBottom + verticalSliderValue, partialTicks, mouseHidden);
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
        this.drawVerticalSlider(poseStack);

        GlGraphicsManager.glPushMatrix(poseStack);

        int translateX = getElementsTranslateX();
        GlGraphicsManager.glTranslate(poseStack, translateX, this.paddingTopBottom - verticalSliderValue, 0);
        this.elementsComponent.drawComponent(poseStack);

        GlGraphicsManager.glPopMatrix(poseStack);
    }


    private void drawVerticalSlider(Object poseStack) {
        int[] dimension = this.getVerticalSliderDimension();
        int color = this.verticalSliderHoverState ? VERTICAL_SLIDER_COLOR_HOVERED : VERTICAL_SLIDER_COLOR;
        if(this.verticalSliderChangingState) color = VERTICAL_SLIDER_COLOR_CLICKED;
        RawGuiManager.fillRect(poseStack,
                dimension[0], dimension[1] + VERTICAL_SLIDER_PADDING,
                dimension[2] - VERTICAL_SLIDER_PADDING, dimension[3] - VERTICAL_SLIDER_PADDING, color);
    }


    private void drawSidebarBackground(Object poseStack) {
        IScaledScreenSize screen = screenSize.get();
        int height = screen.getHeight();

        // Background
        int[] range = this.getSidebarXRange();
        RawGuiManager.fillRect(poseStack, range[0], 0, range[1], height, SIDEBAR_BACKGROUND_COLOR);

        // Width change bar
        int widthChangeBarX = this.getWidthChangeBarX();
        int changeBarColor = this.widthChangeBarHoverState ? WIDTH_CHANGE_BAR_COLOR_HOVERED : WIDTH_CHANGE_BAR_COLOR;
        int changeBarShadow = this.widthChangeBarHoverState ? WIDTH_CHANGE_BAR_SHADOW_HOVERED : WIDTH_CHANGE_BAR_SHADOW;
        RawGuiManager.fillRect(poseStack, widthChangeBarX - 1, 0, widthChangeBarX, height,
                this.side == SidebarSide.LEFT ? changeBarShadow : changeBarColor);
        RawGuiManager.fillRect(poseStack, widthChangeBarX, 0, widthChangeBarX + 1, height,
                this.side == SidebarSide.RIGHT ? changeBarShadow : changeBarColor);
    }


    /**
     * @return [ x1, y1, x2, y2 ]
     */
    private int[] getVerticalSliderDimension() {
        IScaledScreenSize screen = screenSize.get();
        int right = this.getSidebarXRange()[1];
        double screenHeight = screen.getHeight();
        double elementsHeight = this.elementsComponent.getVisualHeight() + 2 * this.paddingTopBottom;
        if(elementsHeight <= screenHeight) return new int[] { 0, 0, 0, -1 };

        double multiplier = screenHeight / elementsHeight;
        int sliderY = (int) (this.verticalSliderValue * multiplier);
        int sliderHeight = (int) (screenHeight * multiplier);

        return new int[] {
                right - VERTICAL_SLIDER_WIDTH, sliderY,
                right, sliderY + sliderHeight
        };
    }


    public boolean mousePressed(double mouseX, double mouseY, int mouseButton) {
        if(this.guiChat.isOpened() && this.guiChat.mouseClickResponse(mouseX, mouseY, mouseButton))
            return true;

        this.initialElementWidth = elementWidth.get();
        this.mouseClickX = mouseX;
        this.mouseClickY = mouseY;
        this.initialVerticalSliderValue = this.verticalSliderValue;

        boolean result = this.elementsComponent.mousePressed(mouseX - this.getElementsTranslateX(),
                mouseY - this.paddingTopBottom + this.verticalSliderValue, mouseButton);
        if(result) return true;

        result = this.verticalSliderChangingState = this.mouseOnScrollBar(mouseX, mouseY);
        if(result) return true;

        return this.widthChangingState = Math.abs(mouseX - this.getWidthChangeBarX()) <= 4;
    }


    public boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
        this.elementsComponent.mouseReleased(mouseX - this.getElementsTranslateX(),
                mouseY - this.paddingTopBottom + this.verticalSliderValue, mouseButton);
        this.widthChangingState = false;
        this.verticalSliderChangingState = false;
        return true;
    }


    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollAmount) {
        if(this.guiChat.isOpened() && !this.mouseOnSidebar(mouseX)) {
            // TODO: Maybe I should consider removing guiChat from sidebar
            this.guiChat.handleMouseInput();
        } else {
            this.verticalSliderValue -= Math.signum(scrollAmount) * 30;
            this.validateSliderValue();
        }
        return true;
    }


    private boolean mouseOnScrollBar(double mouseX, double mouseY) {
        int[] dimension = this.getVerticalSliderDimension();
        return mouseX >= dimension[0] && mouseX <= dimension[2] && mouseY >= dimension[1] && mouseY <= dimension[3];
    }


    private boolean mouseOnSidebar(double mouseX) {
        int[] range = this.getSidebarXRange();
        return mouseX >= range[0] && mouseX <= range[1];
    }


    private void validateSliderValue() {
        if(verticalSliderValue != 0) {
            IScaledScreenSize screen = screenSize.get();

            int totalHeight = this.elementsComponent.getVisualHeight() + 2 * this.paddingTopBottom;
            if (this.verticalSliderValue > totalHeight - screen.getHeight()) {
                this.verticalSliderValue = totalHeight - screen.getHeight();
            }
            if (this.verticalSliderValue < 0) this.verticalSliderValue = 0;
        }
    }


    private int getSidebarWidth() {
        return (int) (elementWidth.get() + 2 * this.elementPaddingSide);
    }

    /**
     * @return [ leftX, rightX ]
     */
    private int[] getSidebarXRange() {
        IScaledScreenSize screen = screenSize.get();
        int screenWidth = screen.getWidth(), sidebarWidth = this.getSidebarWidth();
        return this.side == SidebarSide.LEFT ?
                new int[] { 0, sidebarWidth } : new int[] { screenWidth - sidebarWidth, screenWidth };
    }

    private int getWidthChangeBarX() {
        int[] range = this.getSidebarXRange();
        return this.side == SidebarSide.LEFT ? range[1] : range[0];
    }

    private int getElementsTranslateX() {
        return this.getSidebarXRange()[0] + this.elementPaddingSide;
    }


    public boolean keyTyped(char key, int keyCode) {
        if(this.guiChat.isOpened() && this.guiChat.keyTypedResponse(key, keyCode)) {
            return true;
        }

        boolean keyTyped = this.elementsComponent.keyTyped(key, keyCode);
        if (keyTyped) return true;

        if(this.guiChat.isAvailable()) {
            if(keyCode == MinecraftClientManager.chatOpenKeyCode()) {
                this.guiChat.setText("", true);
                return true;
            }
            else if(keyCode == MinecraftClientManager.commandOpenKeyCode()) {
                this.guiChat.setText("/", true);
                return true;
            }
        }
        return false;
    }


    @Override
    public boolean keyPressed(InputKey key) {
        return this.elementsComponent.keyPressed(key);
    }


    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double pMouseX, double pMouseY) {
        if(this.widthChangingState) {
            double dMouseX = mouseX - mouseClickX;
            if(side == SidebarSide.RIGHT) dMouseX = -dMouseX;
            double newElementWidth = BtrUtil.clamp(initialElementWidth + dMouseX, 130, 270);

            elementWidth.set(newElementWidth);
            this.elementsComponent.onWidthChange(newElementWidth);
            this.guiChat.changeSideMargin(side, (int) newElementWidth + 2 * this.elementPaddingSide);
            return true;
        }
        if(this.verticalSliderChangingState) {
            IScaledScreenSize screen = screenSize.get();
            double totalHeight = this.elementsComponent.getVisualHeight() + 2 * this.paddingTopBottom;
            double screenHeight = screen.getHeight();

            double dMouseY = mouseY - mouseClickY;
            double dValue = dMouseY * totalHeight / (screenHeight - 2);
            double newVerticalSliderValue = BtrUtil.clamp(initialVerticalSliderValue + dValue,
                    0, Math.max(totalHeight - screenHeight, 0));

            this.verticalSliderValue = (int) newVerticalSliderValue;
            return true;
        }

        int translateX = this.getElementsTranslateX();
        int currentHeight = this.paddingTopBottom - verticalSliderValue;
        return this.elementsComponent.mouseDragged(
                mouseX - translateX, mouseY - currentHeight, mouseButton,
                pMouseX - translateX, pMouseY - currentHeight
        );
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
