package com.mndk.bteterrarenderer.gui.sidebar;

import com.mndk.bteterrarenderer.connector.minecraft.GameSettingsConnector;
import com.mndk.bteterrarenderer.connector.Connectors;
import com.mndk.bteterrarenderer.connector.minecraft.gui.FontRendererConnector;
import com.mndk.bteterrarenderer.connector.minecraft.gui.GuiScreenConnector;
import com.mndk.bteterrarenderer.connector.minecraft.graphics.ScaledResolutionConnector;
import com.mndk.bteterrarenderer.util.GetterSetter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GuiSidebar extends GuiScreenConnector {


    protected final GuiScreenConnector parent;
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

    private int initialMouseX = 0, initialElementWidth;


    public GuiSidebar(
            GuiScreenConnector parent, SidebarSide side,
            int paddingSide, int paddingTopBottom, int elementDistance,
            GetterSetter<Integer> elementWidth, boolean guiPausesGame
    ) {
        this.parent = parent;
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
        this.guiChat.initGui(parent.getScaledResolution());
        this.guiChat.changeSideMargin(side, this.elementWidth.get() + 2 * this.paddingSide);
        for(GuiSidebarElement element : elements) {
            if(element == null) continue;
            element.initGui(this, parent.getFontRenderer());
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


    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if(this.guiChat.isOpened()) {
            this.guiChat.drawScreen(mouseX, mouseY, partialTicks);
        }

        this.drawSidebarBackground(mouseX);

        int translateX = getTranslateX();

        Connectors.GRAPHICS.glPushAttrib();
        Connectors.GRAPHICS.glTranslate(translateX, this.paddingTopBottom - verticalSlider, 0);
        int currentHeight = this.paddingTopBottom - verticalSlider;

        this.totalHeight = this.paddingTopBottom;
        for(GuiSidebarElement element : elements) {
            if(element == null) continue;
            element.drawScreen(mouseX - translateX, mouseY - currentHeight, partialTicks);
            Connectors.GRAPHICS.glTranslate(0, element.getHeight() + this.elementDistance, 0);
            currentHeight += element.getHeight() + this.elementDistance;
            this.totalHeight += element.getHeight() + this.elementDistance;
        }
        this.totalHeight += this.paddingTopBottom - this.elementDistance;

        Connectors.GRAPHICS.glPopAttrib();
    }


    private void drawSidebarBackground(int mouseX) {
        ScaledResolutionConnector scaled = parent.getScaledResolution();

        int widthChangeBarX;

        if(this.side == SidebarSide.LEFT) {
            int right = widthChangeBarX = elementWidth.get() + 2 * this.paddingSide;

            Connectors.GUI.drawRect(
                    0, 0,
                    right, scaled.getScaledHeight(),
                    0x5F000000
            );
        }
        else if(this.side == SidebarSide.RIGHT) {
            int left = widthChangeBarX = scaled.getScaledWidth() - elementWidth.get() - 2 * this.paddingSide;

            Connectors.GUI.drawRect(
                    left, 0,
                    scaled.getScaledWidth(), scaled.getScaledHeight(),
                    0x3F000000
            );
        }
        else return;

        Connectors.GUI.drawRect(widthChangeBarX - 1, 0, widthChangeBarX, scaled.getScaledHeight(),
                Math.abs(mouseX - widthChangeBarX) <= 4 ? 0xFFFFFFA0 : 0xFFFFFFFF);

        Connectors.GUI.drawRect(widthChangeBarX, 0, widthChangeBarX + 1, scaled.getScaledHeight(),
                Math.abs(mouseX - widthChangeBarX) <= 4 ? 0xFF3f3f28 : 0xFF383838);
    }


    public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if(this.guiChat.isOpened()) {
            if(this.guiChat.mouseClickResponse(mouseX, mouseY, mouseButton)) {
                return;
            }
        }

        int sidebarWidth = elementWidth.get() + 2 * this.paddingSide;
        if(side == SidebarSide.RIGHT) {
            sidebarWidth = parent.getScaledResolution().getScaledWidth() - sidebarWidth;
        }

        if(Math.abs(mouseX - sidebarWidth) <= 4) {
            widthChangingState = true;
        }

        if(!widthChangingState) {
            int mx = mouseX - this.getTranslateX();

            int currentHeight = this.paddingTopBottom - verticalSlider;
            for (GuiSidebarElement element : elements) {
                if (element == null) continue;
                if(element.mouseClicked(mx, mouseY - currentHeight, mouseButton)) {
                    Connectors.SOUND.playClickSound();
                }
                currentHeight += element.getHeight() + this.elementDistance;
            }
        }

        this.initialMouseX = mouseX; this.initialElementWidth = elementWidth.get();
    }


    public void mouseReleased(int mouseX, int mouseY, int state) {
        int mx = mouseX - this.getTranslateX();

        int currentHeight = this.paddingTopBottom - verticalSlider;
        for(GuiSidebarElement element : elements) {
            if(element == null) continue;
            element.mouseReleased(mx, mouseY - currentHeight, state);
            currentHeight += element.getHeight() + this.elementDistance;
        }

        widthChangingState = false;
    }


    public void handleMouseInput() throws IOException {
        int mouseX = (int) (Connectors.MOUSE.getEventX() * parent.getWidth() / (double) parent.minecraftDisplayWidth());
        if(this.guiChat.isOpened() && !this.mouseOnSidebar(mouseX)) {
            this.guiChat.handleMouseInput();
        } else {
            this.verticalSlider -= Math.signum(Connectors.MOUSE.getEventDWheel()) * 30;
            this.validateVerticalSlider();
        }
    }


    private boolean mouseOnSidebar(int mouseX) {
        ScaledResolutionConnector scaled = parent.getScaledResolution();
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
            ScaledResolutionConnector scaled = parent.getScaledResolution();

            if (this.verticalSlider > this.totalHeight - scaled.getScaledHeight()) {
                this.verticalSlider = this.totalHeight - scaled.getScaledHeight();
            }
            if (this.verticalSlider < 0) this.verticalSlider = 0;
        }
    }


    private int getTranslateX() {
        ScaledResolutionConnector scaled = parent.getScaledResolution();

        if(this.side == SidebarSide.LEFT) {
            return this.paddingSide;
        }
        else {
            return scaled.getScaledWidth() - elementWidth.get() - this.paddingSide;
        }
    }


    public void keyTyped(char key, int keyCode) throws IOException {
        if(this.guiChat.isOpened() && this.guiChat.keyTypedResponse(key, keyCode)) {
            return;
        }

        boolean response = false;
        for (GuiSidebarElement element : elements) {
            if (element == null) continue;
            if (element.keyTyped(key, keyCode)) response = true;
        }
        if (!response) {
            GameSettingsConnector gameSettings = Connectors.GAME_SETTINGS;
            if(keyCode == gameSettings.getKeyBindChatCode()) {
                this.guiChat.setText("", true);
            }
            else if(keyCode == gameSettings.getKeyBindCommandCode()) {
                this.guiChat.setText("/", true);
            }
            else {
                return;
            }
            this.guiChat.setOpened(true);
        }
    }

    @Override
    public int getWidth() {
        return parent.getWidth();
    }

    @Override
    public int minecraftDisplayWidth() {
        return parent.minecraftDisplayWidth();
    }

    @Override
    public ScaledResolutionConnector getScaledResolution() {
        return parent.getScaledResolution();
    }

    @Override
    public FontRendererConnector getFontRenderer() {
        return parent.getFontRenderer();
    }


    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if(widthChangingState) {
            int dMouseX = mouseX - initialMouseX;
            int newElementWidth = initialElementWidth + (side == SidebarSide.LEFT ? dMouseX : -dMouseX);
            if(newElementWidth > 270) newElementWidth = 270;
            if(newElementWidth < 130) newElementWidth = 130;

            elementWidth.set(newElementWidth);

            for(GuiSidebarElement element : elements) {
                if(element == null) continue;
                element.onWidthChange(elementWidth.get());
            }

            this.guiChat.changeSideMargin(side, newElementWidth + 2 * this.paddingSide);
        }
        else {
            int mx = mouseX - this.getTranslateX();
            int currentHeight = this.paddingTopBottom - verticalSlider;
            for (GuiSidebarElement element : elements) {
                if (element == null) continue;
                element.mouseClickMove(mx, mouseY - currentHeight, clickedMouseButton, timeSinceLastClick);
                currentHeight += element.getHeight() + this.elementDistance;
            }
        }
    }


    public void onGuiClosed() {
        this.guiChat.setOpened(false);
    }

    public boolean doesGuiPauseGame() {
        return guiPausesGame;
    }
}
