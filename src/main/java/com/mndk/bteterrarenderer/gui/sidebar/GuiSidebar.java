package com.mndk.bteterrarenderer.gui.sidebar;

import com.mndk.bteterrarenderer.util.GetterSetter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.init.SoundEvents;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GuiSidebar extends GuiScreen {


    protected final List<GuiSidebarElement> elements;
    private SidebarSide side;
    private final int paddingSide;
    private final int paddingTopBottom;
    private final int elementDistance;
    private int verticalSlider;

    public final GetterSetter<Integer> elementWidth;

    private int totalHeight;
    private boolean widthChangingState;

    private final SidebarGuiChat guiChat;

    private int initialMouseX = 0, initialElementWidth;


    public GuiSidebar(
            SidebarSide side,
            int paddingSide, int paddingTopBottom, int elementDistance,
            GetterSetter<Integer> elementWidth
    ) {
        this.elements = new ArrayList<>();
        this.side = side;
        this.paddingSide = paddingSide;
        this.paddingTopBottom = paddingTopBottom;
        this.elementDistance = elementDistance;
        this.verticalSlider = 0;
        this.widthChangingState = false;
        this.guiChat = new SidebarGuiChat();

        this.elementWidth = elementWidth;
    }


    public void setSide(SidebarSide side) {
        this.side = side;
    }


    @Override
    public void initGui() {
        super.initGui();
        this.guiChat.initGui(this.mc, this.getScaledResolution());
        this.guiChat.changeSideMargin(side, this.elementWidth.get() + 2 * this.paddingSide);
        for(GuiSidebarElement element : elements) {
            if(element == null) continue;
            element.initGui(this, this.fontRenderer);
        }
    }


    @Override
    public void updateScreen() {
        super.updateScreen();
        for(GuiSidebarElement element : elements) {
            if(element == null) continue;
            element.updateScreen();
        }
        if(this.guiChat.isOpened()) {
            this.guiChat.updateScreen();
        }
    }


    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        if(this.guiChat.isOpened()) {
            this.guiChat.drawScreen(mouseX, mouseY, partialTicks);
        }

        this.drawSidebarBackground(mouseX);

        int translateX = getTranslateX();

        GlStateManager.pushAttrib();
        GlStateManager.translate(translateX, this.paddingTopBottom - verticalSlider, 0);
        int currentHeight = this.paddingTopBottom - verticalSlider;

        this.totalHeight = this.paddingTopBottom;
        for(GuiSidebarElement element : elements) {
            if(element == null) continue;
            element.drawScreen(mouseX - translateX, mouseY - currentHeight, partialTicks);
            GlStateManager.translate(0, element.getHeight() + this.elementDistance, 0);
            currentHeight += element.getHeight() + this.elementDistance;
            this.totalHeight += element.getHeight() + this.elementDistance;
        }
        this.totalHeight += this.paddingTopBottom - this.elementDistance;

        GlStateManager.popAttrib();
    }


    private void drawSidebarBackground(int mouseX) {
        ScaledResolution scaled = this.getScaledResolution();

        int widthChangeBarX;

        if(this.side == SidebarSide.LEFT) {
            int right = widthChangeBarX = elementWidth.get() + 2 * this.paddingSide;

            Gui.drawRect(
                    0, 0,
                    right, scaled.getScaledHeight(),
                    0x5F000000
            );
        }
        else if(this.side == SidebarSide.RIGHT) {
            int left = widthChangeBarX = scaled.getScaledWidth() - elementWidth.get() - 2 * this.paddingSide;

            Gui.drawRect(
                    left, 0,
                    scaled.getScaledWidth(), scaled.getScaledHeight(),
                    0x3F000000
            );
        }
        else return;

        Gui.drawRect(widthChangeBarX - 1, 0, widthChangeBarX, scaled.getScaledHeight(),
                Math.abs(mouseX - widthChangeBarX) <= 4 ? 0xFFFFFFA0 : 0xFFFFFFFF);

        Gui.drawRect(widthChangeBarX, 0, widthChangeBarX + 1, scaled.getScaledHeight(),
                Math.abs(mouseX - widthChangeBarX) <= 4 ? 0xFF3f3f28 : 0xFF383838);
    }


    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        if(this.guiChat.isOpened()) {
            if(this.guiChat.mouseClickResponse(mouseX, mouseY, mouseButton)) {
                return;
            }
        }

        int sidebarWidth = elementWidth.get() + 2 * this.paddingSide;
        if(side == SidebarSide.RIGHT) {
            sidebarWidth = this.getScaledResolution().getScaledWidth() - sidebarWidth;
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
                    this.mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(
                            SoundEvents.UI_BUTTON_CLICK, 1.0F
                    ));
                }
                currentHeight += element.getHeight() + this.elementDistance;
            }
        }

        this.initialMouseX = mouseX; this.initialElementWidth = elementWidth.get();
    }


    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);

        int mx = mouseX - this.getTranslateX();

        int currentHeight = this.paddingTopBottom - verticalSlider;
        for(GuiSidebarElement element : elements) {
            if(element == null) continue;
            element.mouseReleased(mx, mouseY - currentHeight, state);
            currentHeight += element.getHeight() + this.elementDistance;
        }

        widthChangingState = false;
    }


    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        if(this.guiChat.isOpened() && !this.mouseOnSidebar(Mouse.getEventX() * this.width / this.mc.displayWidth)) {
            this.guiChat.handleMouseInput();
        } else {
            this.verticalSlider -= Math.signum(Mouse.getEventDWheel()) * 30;
            this.validateVerticalSlider();
        }
    }


    private boolean mouseOnSidebar(int mouseX) {
        ScaledResolution scaled = this.getScaledResolution();
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
            ScaledResolution scaled = this.getScaledResolution();

            if (this.verticalSlider > this.totalHeight - scaled.getScaledHeight()) {
                this.verticalSlider = this.totalHeight - scaled.getScaledHeight();
            }
            if (this.verticalSlider < 0) this.verticalSlider = 0;
        }
    }


    private int getTranslateX() {
        ScaledResolution scaled = this.getScaledResolution();

        if(this.side == SidebarSide.LEFT) {
            return this.paddingSide;
        }
        else {
            return scaled.getScaledWidth() - elementWidth.get() - this.paddingSide;
        }
    }


    @Override
    protected void keyTyped(char key, int keyCode) throws IOException {
        super.keyTyped(key, keyCode);
        if(this.guiChat.isOpened() && this.guiChat.keyTypedResponse(key, keyCode)) {
            return;
        }

        boolean response = false;
        for (GuiSidebarElement element : elements) {
            if (element == null) continue;
            if (element.keyTyped(key, keyCode)) response = true;
        }
        if (!response) {
            GameSettings gameSettings = Minecraft.getMinecraft().gameSettings;
            if(gameSettings.keyBindChat.getKeyCode() == keyCode) {
                this.guiChat.setText("", true);
            }
            else if(gameSettings.keyBindCommand.getKeyCode() == keyCode) {
                this.guiChat.setText("/", true);
            }
            else {
                return;
            }
            this.guiChat.setOpened(true);
        }
    }


    @Override
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


    @Override
    public void onGuiClosed() {
        this.guiChat.setOpened(false);
    }


    public ScaledResolution getScaledResolution() {
        return new ScaledResolution(this.mc);
    }

}
