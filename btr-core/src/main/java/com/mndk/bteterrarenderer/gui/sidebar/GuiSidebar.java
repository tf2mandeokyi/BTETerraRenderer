package com.mndk.bteterrarenderer.gui.sidebar;

import com.mndk.bteterrarenderer.connector.graphics.GraphicsConnector;
import com.mndk.bteterrarenderer.connector.graphics.IScaledScreenSize;
import com.mndk.bteterrarenderer.connector.gui.GuiStaticConnector;
import com.mndk.bteterrarenderer.connector.minecraft.InputKey;
import com.mndk.bteterrarenderer.connector.minecraft.MinecraftClientConnector;
import com.mndk.bteterrarenderer.gui.components.AbstractGuiScreen;
import com.mndk.bteterrarenderer.util.GetterSetter;

import java.util.ArrayList;
import java.util.List;

public class GuiSidebar extends AbstractGuiScreen {

    protected final List<GuiSidebarElement> elements;
    private SidebarSide side;
    private final int paddingSide;
    private final int paddingTopBottom;
    private final int elementDistance;
    private final boolean guiPausesGame;
    private int verticalSlider;

    public final GetterSetter<Double> elementWidth;
    private double mouseClickX, initialElementWidth;

    private int totalHeight;
    private boolean widthChangingState;

    private final SidebarGuiChat guiChat;


    public GuiSidebar(
            SidebarSide side, int paddingSide, int paddingTopBottom, int elementDistance,
            GetterSetter<Double> elementWidth, boolean guiPausesGame
    ) {
        this.elements = new ArrayList<>();
        this.side = side;
        this.paddingSide = paddingSide;
        this.paddingTopBottom = paddingTopBottom;
        this.elementDistance = elementDistance;
        this.verticalSlider = 0;
        this.widthChangingState = false;
        this.guiPausesGame = guiPausesGame;
        this.guiChat = new SidebarGuiChat();

        this.elementWidth = elementWidth;
    }


    public void setSide(SidebarSide side) {
        this.side = side;
    }

    public void initGui() {
        this.guiChat.initGui(screenSize.get());
        this.guiChat.changeSideMargin(side, (int) (this.elementWidth.get() + 2 * this.paddingSide));
        for(GuiSidebarElement element : elements) {
            if(element == null) continue;
            element.initGui(this);
        }
    }


    public void updateScreen() {
        for(GuiSidebarElement element : elements) {
            if(element == null) continue;
            element.updateScreen();
        }
        if(this.guiChat.isOpened()) {
            this.guiChat.updateScreen();
        }
    }


    public void drawScreen(Object poseStack, double mouseX, double mouseY, float partialTicks) {
        if(this.guiChat.isOpened()) {
            this.guiChat.drawScreen(poseStack, mouseX, mouseY, partialTicks);
        }

        this.drawSidebarBackground(poseStack, mouseX);

        int translateX = getTranslateX();

        GraphicsConnector.INSTANCE.glPushMatrix(poseStack);
        GraphicsConnector.INSTANCE.glTranslate(poseStack, translateX, this.paddingTopBottom - verticalSlider, 0);
        int currentHeight = this.paddingTopBottom - verticalSlider;

        this.totalHeight = this.paddingTopBottom;
        for(GuiSidebarElement element : elements) {
            if(element == null) continue;
            element.drawComponent(poseStack, mouseX - translateX, mouseY - currentHeight, partialTicks);
            GraphicsConnector.INSTANCE.glTranslate(poseStack, 0, element.getHeight() + this.elementDistance, 0);
            currentHeight += element.getHeight() + this.elementDistance;
            this.totalHeight += element.getHeight() + this.elementDistance;
        }
        this.totalHeight += this.paddingTopBottom - this.elementDistance;

        GraphicsConnector.INSTANCE.glPopMatrix(poseStack);
    }


    private void drawSidebarBackground(Object poseStack, double mouseX) {
        IScaledScreenSize screen = screenSize.get();

        int widthChangeBarX;

        if(this.side == SidebarSide.LEFT) {
            int right = widthChangeBarX = (int) (elementWidth.get() + 2 * this.paddingSide);

            GuiStaticConnector.INSTANCE.fillRect(poseStack,
                    0, 0,
                    right, screen.getHeight(),
                    0x5F000000
            );
        }
        else if(this.side == SidebarSide.RIGHT) {
            int left = widthChangeBarX = (int) (screen.getWidth() - elementWidth.get() - 2 * this.paddingSide);

            GuiStaticConnector.INSTANCE.fillRect(poseStack,
                    left, 0,
                    screen.getWidth(), screen.getHeight(),
                    0x3F000000
            );
        }
        else return;

        GuiStaticConnector.INSTANCE.fillRect(poseStack, widthChangeBarX - 1, 0, widthChangeBarX, screen.getHeight(),
                Math.abs(mouseX - widthChangeBarX) <= 4 ? 0xFFFFFFA0 : 0xFFFFFFFF);

        GuiStaticConnector.INSTANCE.fillRect(poseStack, widthChangeBarX, 0, widthChangeBarX + 1, screen.getHeight(),
                Math.abs(mouseX - widthChangeBarX) <= 4 ? 0xFF3f3f28 : 0xFF383838);
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
            double mx = mouseX - this.getTranslateX();

            int currentHeight = this.paddingTopBottom - verticalSlider;
            for (GuiSidebarElement element : elements) {
                if (element == null) continue;
                if(element.mousePressed(mx, mouseY - currentHeight, mouseButton)) {
                    MinecraftClientConnector.INSTANCE.playClickSound();
                    clicked = true;
                }
                currentHeight += element.getHeight() + this.elementDistance;
            }
        }

        this.initialElementWidth = elementWidth.get();
        this.mouseClickX = mouseX;
        return clicked;
    }


    public boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
        double mx = mouseX - this.getTranslateX();

        int currentHeight = this.paddingTopBottom - verticalSlider;
        for(GuiSidebarElement element : elements) {
            if(element == null) continue;
            element.mouseReleased(mx, mouseY - currentHeight, mouseButton);
            currentHeight += element.getHeight() + this.elementDistance;
        }

        widthChangingState = false;
        return true;
    }


    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollAmount) {
        if(this.guiChat.isOpened() && !this.mouseOnSidebar(mouseX)) {
            // TODO: Maybe I should consider removing guiChat from sidebar
            this.guiChat.handleMouseInput();
        } else {
            this.verticalSlider -= Math.signum(scrollAmount) * 30;
            this.validateVerticalSlider();
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


    private void validateVerticalSlider() {
        if(verticalSlider != 0) {
            IScaledScreenSize screen = screenSize.get();

            if (this.verticalSlider > this.totalHeight - screen.getHeight()) {
                this.verticalSlider = this.totalHeight - screen.getHeight();
            }
            if (this.verticalSlider < 0) this.verticalSlider = 0;
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

        boolean keyTyped = false;
        for (GuiSidebarElement element : elements) {
            if (element == null) continue;
            if (element.keyTyped(key, keyCode)) keyTyped = true;
        }
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
        boolean keyPressed = false;
        for (GuiSidebarElement element : elements) {
            if (element == null) continue;
            if (element.keyPressed(key)) keyPressed = true;
        }
        return keyPressed;
    }


    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double pMouseX, double pMouseY) {
        if(widthChangingState) {
            double dMouseX = mouseX - mouseClickX;
            double newElementWidth = initialElementWidth + (side == SidebarSide.LEFT ? dMouseX : -dMouseX);
            if(newElementWidth > 270) newElementWidth = 270;
            if(newElementWidth < 130) newElementWidth = 130;

            elementWidth.set(newElementWidth);

            for(GuiSidebarElement element : elements) {
                if(element == null) continue;
                element.onWidthChange(elementWidth.get());
            }

            this.guiChat.changeSideMargin(side, (int) newElementWidth + 2 * this.paddingSide);
            return true;
        }
        else {
            int tx = this.getTranslateX();
            int currentHeight = this.paddingTopBottom - verticalSlider;
            boolean dragged = false;
            for (GuiSidebarElement element : elements) {
                if (element == null) continue;
                boolean result = element.mouseDragged(
                        mouseX - tx, mouseY - currentHeight, mouseButton,
                        pMouseX - tx, pMouseY - currentHeight
                );
                if(result) dragged = true;
                currentHeight += element.getHeight() + this.elementDistance;
            }
            return dragged;
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
