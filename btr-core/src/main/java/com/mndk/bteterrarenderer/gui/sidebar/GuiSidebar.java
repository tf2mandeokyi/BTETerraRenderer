package com.mndk.bteterrarenderer.gui.sidebar;

import com.mndk.bteterrarenderer.connector.minecraft.InputKey;
import com.mndk.bteterrarenderer.connector.graphics.GraphicsConnector;
import com.mndk.bteterrarenderer.connector.graphics.IScaledResolution;
import com.mndk.bteterrarenderer.gui.components.AbstractGuiScreen;
import com.mndk.bteterrarenderer.connector.gui.GuiStaticConnector;
import com.mndk.bteterrarenderer.connector.minecraft.MinecraftClientConnector;
import com.mndk.bteterrarenderer.util.GetterSetter;

import java.io.IOException;
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

    public final GetterSetter<Integer> elementWidth;

    private int totalHeight;
    private boolean widthChangingState;

    private final SidebarGuiChat guiChat;

    private int initialElementWidth;


    public GuiSidebar(
            SidebarSide side, int paddingSide, int paddingTopBottom, int elementDistance,
            GetterSetter<Integer> elementWidth, boolean guiPausesGame
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
        this.guiChat.initGui(scaledResolution.get());
        this.guiChat.changeSideMargin(side, this.elementWidth.get() + 2 * this.paddingSide);
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


    public void drawScreen(double mouseX, double mouseY, float partialTicks) {
        if(this.guiChat.isOpened()) {
            this.guiChat.drawScreen(mouseX, mouseY, partialTicks);
        }

        this.drawSidebarBackground(mouseX);

        int translateX = getTranslateX();

        GraphicsConnector.INSTANCE.glPushAttrib();
        GraphicsConnector.INSTANCE.glTranslate(translateX, this.paddingTopBottom - verticalSlider, 0);
        int currentHeight = this.paddingTopBottom - verticalSlider;

        this.totalHeight = this.paddingTopBottom;
        for(GuiSidebarElement element : elements) {
            if(element == null) continue;
            element.drawComponent(mouseX - translateX, mouseY - currentHeight, partialTicks);
            GraphicsConnector.INSTANCE.glTranslate(0, element.getHeight() + this.elementDistance, 0);
            currentHeight += element.getHeight() + this.elementDistance;
            this.totalHeight += element.getHeight() + this.elementDistance;
        }
        this.totalHeight += this.paddingTopBottom - this.elementDistance;

        GraphicsConnector.INSTANCE.glPopAttrib();
    }


    private void drawSidebarBackground(double mouseX) {
        IScaledResolution scaled = scaledResolution.get();

        int widthChangeBarX;

        if(this.side == SidebarSide.LEFT) {
            int right = widthChangeBarX = elementWidth.get() + 2 * this.paddingSide;

            GuiStaticConnector.INSTANCE.drawRect(
                    0, 0,
                    right, scaled.getScaledHeight(),
                    0x5F000000
            );
        }
        else if(this.side == SidebarSide.RIGHT) {
            int left = widthChangeBarX = scaled.getScaledWidth() - elementWidth.get() - 2 * this.paddingSide;

            GuiStaticConnector.INSTANCE.drawRect(
                    left, 0,
                    scaled.getScaledWidth(), scaled.getScaledHeight(),
                    0x3F000000
            );
        }
        else return;

        GuiStaticConnector.INSTANCE.drawRect(widthChangeBarX - 1, 0, widthChangeBarX, scaled.getScaledHeight(),
                Math.abs(mouseX - widthChangeBarX) <= 4 ? 0xFFFFFFA0 : 0xFFFFFFFF);

        GuiStaticConnector.INSTANCE.drawRect(widthChangeBarX, 0, widthChangeBarX + 1, scaled.getScaledHeight(),
                Math.abs(mouseX - widthChangeBarX) <= 4 ? 0xFF3f3f28 : 0xFF383838);
    }


    public boolean mousePressed(double mouseX, double mouseY, int mouseButton) {
        if(this.guiChat.isOpened() && this.guiChat.mouseClickResponse(mouseX, mouseY, mouseButton)) return true;

        int sidebarWidth = elementWidth.get() + 2 * this.paddingSide;
        if(side == SidebarSide.RIGHT) {
            sidebarWidth = scaledResolution.get().getScaledWidth() - sidebarWidth;
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
        return clicked;
    }


    public void mouseReleased(double mouseX, double mouseY, int mouseButton) {
        double mx = mouseX - this.getTranslateX();

        int currentHeight = this.paddingTopBottom - verticalSlider;
        for(GuiSidebarElement element : elements) {
            if(element == null) continue;
            element.mouseReleased(mx, mouseY - currentHeight, mouseButton);
            currentHeight += element.getHeight() + this.elementDistance;
        }

        widthChangingState = false;
    }


    public void handleMouseInput() throws IOException {
        int mouseX = (int) (MinecraftClientConnector.INSTANCE.getMouseX() * guiWidth.get() / (double) minecraftDisplayWidth.get());
        if(this.guiChat.isOpened() && !this.mouseOnSidebar(mouseX)) {
            this.guiChat.handleMouseInput();
        } else {
            this.verticalSlider -= Math.signum(MinecraftClientConnector.INSTANCE.getMouseDWheel()) * 30;
            this.validateVerticalSlider();
        }
    }


    private boolean mouseOnSidebar(int mouseX) {
        IScaledResolution scaled = scaledResolution.get();
        int left, right;

        if(this.side == SidebarSide.LEFT) {
            left = 0;
            right =  elementWidth.get() + 2 * this.paddingSide;
        }
        else {
            left = scaled.getScaledWidth() - elementWidth.get() - 2 * this.paddingSide;
            right = scaled.getScaledWidth();
        }
        return mouseX >= left && mouseX <= right;
    }


    private void validateVerticalSlider() {
        if(verticalSlider != 0) {
            IScaledResolution scaled = scaledResolution.get();

            if (this.verticalSlider > this.totalHeight - scaled.getScaledHeight()) {
                this.verticalSlider = this.totalHeight - scaled.getScaledHeight();
            }
            if (this.verticalSlider < 0) this.verticalSlider = 0;
        }
    }


    private int getTranslateX() {
        IScaledResolution scaled = scaledResolution.get();

        if(this.side == SidebarSide.LEFT) {
            return this.paddingSide;
        }
        else {
            return scaled.getScaledWidth() - elementWidth.get() - this.paddingSide;
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
        if (!keyTyped) {
            if(keyCode == MinecraftClientConnector.INSTANCE.chatOpenKeyCode()) {
                this.guiChat.setText("", true);
            }
            else if(keyCode == MinecraftClientConnector.INSTANCE.commandOpenKeyCode()) {
                this.guiChat.setText("/", true);
            }
            else {
                return false;
            }
            this.guiChat.setOpened(true);
        }
        return true;
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
    public void mouseDragged(double mouseX, double mouseY, int clickedMouseButton, double pMouseX, double pMouseY) {
        if(widthChangingState) {
            double dMouseX = mouseX - pMouseX;
            double newElementWidth = initialElementWidth + (side == SidebarSide.LEFT ? dMouseX : -dMouseX);
            if(newElementWidth > 270) newElementWidth = 270;
            if(newElementWidth < 130) newElementWidth = 130;

            elementWidth.set((int) newElementWidth);

            for(GuiSidebarElement element : elements) {
                if(element == null) continue;
                element.onWidthChange(elementWidth.get());
            }

            this.guiChat.changeSideMargin(side, (int) newElementWidth + 2 * this.paddingSide);
        }
        else {
            int tx = this.getTranslateX();
            int currentHeight = this.paddingTopBottom - verticalSlider;
            for (GuiSidebarElement element : elements) {
                if (element == null) continue;
                element.mouseDragged(
                        mouseX - tx, mouseY - currentHeight, clickedMouseButton,
                        pMouseX - tx, pMouseY - currentHeight
                );
                currentHeight += element.getHeight() + this.elementDistance;
            }
        }
    }

    @Override
    public void onGuiClosed() {
        this.guiChat.setOpened(false);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return guiPausesGame;
    }
}
