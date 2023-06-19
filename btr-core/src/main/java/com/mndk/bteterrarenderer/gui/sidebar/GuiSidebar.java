package com.mndk.bteterrarenderer.gui.sidebar;

import com.mndk.bteterrarenderer.connector.graphics.GraphicsConnector;
import com.mndk.bteterrarenderer.connector.graphics.IScaledScreenSize;
import com.mndk.bteterrarenderer.connector.gui.GuiStaticConnector;
import com.mndk.bteterrarenderer.connector.minecraft.InputKey;
import com.mndk.bteterrarenderer.connector.minecraft.MinecraftClientConnector;
import com.mndk.bteterrarenderer.gui.components.AbstractGuiScreen;
import com.mndk.bteterrarenderer.util.PropertyAccessor;

// TODO: Add tab key
// TODO: Add scroll bar
public class GuiSidebar extends AbstractGuiScreen {

    private static final int SIDEBAR_BACKGROUND_COLOR = 0x5F000000;
    private static final int WIDTH_CHANGE_BAR_COLOR = 0xFFFFFFFF;
    private static final int WIDTH_CHANGE_BAR_COLOR_HOVERED = HOVERED_COLOR;
    private static final int WIDTH_CHANGE_BAR_SHADOW = 0xFF383838;
    private static final int WIDTH_CHANGE_BAR_SHADOW_HOVERED = 0xFF3f3f28;

    protected final SidebarElementListComponent elementsComponent;
    private SidebarSide side;
    private final int paddingSide;
    private final int paddingTopBottom;
    private final boolean guiPausesGame;
    private int sliderValue;

    public final PropertyAccessor<Double> elementWidth;
    private double mouseClickX, initialElementWidth;

    private boolean widthChangingState, widthChangeBarHoverState;

    private final SidebarGuiChat guiChat;


    public GuiSidebar(
            SidebarSide side, int paddingSide, int paddingTopBottom, int elementDistance,
            PropertyAccessor<Double> elementWidth, boolean guiPausesGame
    ) {
        this.elementsComponent = new SidebarElementListComponent(elementDistance);
        this.side = side;
        this.paddingSide = paddingSide;
        this.paddingTopBottom = paddingTopBottom;
        this.sliderValue = 0;
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
        this.guiChat.changeSideMargin(side, (int) (this.elementWidth.get() + 2 * this.paddingSide));
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
        int translateX = getTranslateX();
        boolean result = this.elementsComponent.mouseHovered(mouseX - translateX,
                mouseY - this.paddingTopBottom + sliderValue, partialTicks, mouseHidden);
        if(result) return true;

        IScaledScreenSize screen = screenSize.get();

        int widthChangeBarX;
        if(this.side == SidebarSide.LEFT) {
            widthChangeBarX = (int) (elementWidth.get() + 2 * this.paddingSide);
        }
        else if(this.side == SidebarSide.RIGHT) {
            widthChangeBarX = (int) (screen.getWidth() - elementWidth.get() - 2 * this.paddingSide);
        }
        else return this.widthChangeBarHoverState = false;

        return this.widthChangeBarHoverState = Math.abs(mouseX - widthChangeBarX) <= 4;
    }

    @Override
    protected void drawScreen(Object poseStack) {
        if(this.guiChat.isOpened()) {
            this.guiChat.drawScreen(poseStack);
        }

        this.validateSliderValue();

        this.drawSidebarBackground(poseStack);

        int translateX = getTranslateX();
        GraphicsConnector.INSTANCE.glPushMatrix(poseStack);
        GraphicsConnector.INSTANCE.glTranslate(poseStack, translateX, this.paddingTopBottom - sliderValue, 0);

        this.elementsComponent.drawComponent(poseStack);

        GraphicsConnector.INSTANCE.glPopMatrix(poseStack);
    }


    private void drawSidebarBackground(Object poseStack) {
        IScaledScreenSize screen = screenSize.get();

        int widthChangeBarX;

        if(this.side == SidebarSide.LEFT) {
            int right = widthChangeBarX = (int) (elementWidth.get() + 2 * this.paddingSide);

            GuiStaticConnector.INSTANCE.fillRect(poseStack,
                    0, 0,
                    right, screen.getHeight(),
                    SIDEBAR_BACKGROUND_COLOR
            );
        }
        else if(this.side == SidebarSide.RIGHT) {
            int left = widthChangeBarX = (int) (screen.getWidth() - elementWidth.get() - 2 * this.paddingSide);

            GuiStaticConnector.INSTANCE.fillRect(poseStack,
                    left, 0,
                    screen.getWidth(), screen.getHeight(),
                    SIDEBAR_BACKGROUND_COLOR
            );
        }
        else return;

        GuiStaticConnector.INSTANCE.fillRect(poseStack, widthChangeBarX - 1, 0, widthChangeBarX, screen.getHeight(),
                this.widthChangeBarHoverState ? WIDTH_CHANGE_BAR_COLOR_HOVERED : WIDTH_CHANGE_BAR_COLOR);

        GuiStaticConnector.INSTANCE.fillRect(poseStack, widthChangeBarX, 0, widthChangeBarX + 1, screen.getHeight(),
                this.widthChangeBarHoverState ? WIDTH_CHANGE_BAR_SHADOW_HOVERED : WIDTH_CHANGE_BAR_SHADOW);
    }


    public boolean mousePressed(double mouseX, double mouseY, int mouseButton) {
        if(this.guiChat.isOpened() && this.guiChat.mouseClickResponse(mouseX, mouseY, mouseButton)) return true;

        double sidebarWidth = elementWidth.get() + 2 * this.paddingSide;
        if(side == SidebarSide.RIGHT) {
            sidebarWidth = screenSize.get().getWidth() - sidebarWidth;
        }

        if(Math.abs(mouseX - sidebarWidth) <= 4) {
            widthChangingState = true;
        }

        boolean clicked = false;
        if(!widthChangingState) {
            clicked = this.elementsComponent.mousePressed(mouseX - this.getTranslateX(),
                    mouseY - this.paddingTopBottom + this.sliderValue, mouseButton);
        }

        this.initialElementWidth = elementWidth.get();
        this.mouseClickX = mouseX;
        return clicked;
    }


    public boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
        this.elementsComponent.mouseReleased(mouseX - this.getTranslateX(),
                mouseY - this.paddingTopBottom + this.sliderValue, mouseButton);
        widthChangingState = false;
        return true;
    }


    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollAmount) {
        if(this.guiChat.isOpened() && !this.mouseOnSidebar(mouseX)) {
            // TODO: Maybe I should consider removing guiChat from sidebar
            this.guiChat.handleMouseInput();
        } else {
            this.sliderValue -= Math.signum(scrollAmount) * 30;
            this.validateSliderValue();
        }
        return true;
    }


    private boolean mouseOnSidebar(double mouseX) {
        IScaledScreenSize screen = screenSize.get();
        double left, right;

        if(this.side == SidebarSide.LEFT) {
            left = 0;
            right = elementWidth.get() + 2 * this.paddingSide;
        }
        else {
            left = screen.getWidth() - elementWidth.get() - 2 * this.paddingSide;
            right = screen.getWidth();
        }
        return mouseX >= left && mouseX <= right;
    }


    private void validateSliderValue() {
        if(sliderValue != 0) {
            IScaledScreenSize screen = screenSize.get();

            int totalHeight = this.elementsComponent.getVisualHeight() + 2 * this.paddingTopBottom;
            if (this.sliderValue > totalHeight - screen.getHeight()) {
                this.sliderValue = totalHeight - screen.getHeight();
            }
            if (this.sliderValue < 0) this.sliderValue = 0;
        }
    }


    private int getTranslateX() {
        IScaledScreenSize screen = screenSize.get();

        if(this.side == SidebarSide.LEFT) {
            return this.paddingSide;
        }
        else {
            return (int) (screen.getWidth() - elementWidth.get() - this.paddingSide);
        }
    }


    public boolean keyTyped(char key, int keyCode) {
        if(this.guiChat.isOpened() && this.guiChat.keyTypedResponse(key, keyCode)) {
            return true;
        }

        boolean keyTyped = this.elementsComponent.keyTyped(key, keyCode);
        if (keyTyped) return true;

        if(this.guiChat.isAvailable()) {
            if(keyCode == MinecraftClientConnector.INSTANCE.chatOpenKeyCode()) {
                this.guiChat.setText("", true);
                return true;
            }
            else if(keyCode == MinecraftClientConnector.INSTANCE.commandOpenKeyCode()) {
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
        if(widthChangingState) {
            double dMouseX = mouseX - mouseClickX;
            double newElementWidth = initialElementWidth + (side == SidebarSide.LEFT ? dMouseX : -dMouseX);
            if(newElementWidth > 270) newElementWidth = 270;
            if(newElementWidth < 130) newElementWidth = 130;

            elementWidth.set(newElementWidth);
            this.elementsComponent.onWidthChange(newElementWidth);
            this.guiChat.changeSideMargin(side, (int) newElementWidth + 2 * this.paddingSide);
            return true;
        }
        else {
            int translateX = this.getTranslateX();
            int currentHeight = this.paddingTopBottom - sliderValue;
            return this.elementsComponent.mouseDragged(
                    mouseX - translateX, mouseY - currentHeight, mouseButton,
                    pMouseX - translateX, pMouseY - currentHeight
            );
        }
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
