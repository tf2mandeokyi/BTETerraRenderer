package com.mndk.bteterrarenderer.gui.sidebar;

import com.mndk.bteterrarenderer.util.GetterSetter;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Mouse;

import java.io.IOException;

public class GuiSidebar extends GuiScreen {



    private final GuiSidebarElement[] elements;
    public final SidebarSide side;
    public final int paddingSide;
    public final int paddingTopBottom;
    public final int elementDistance;
    public int verticalSlider;

    public GetterSetter<Integer> elementWidth;

    private int totalHeight;
    private boolean widthChangingState;



    public GuiSidebar(
            GuiSidebarElement[] elements, SidebarSide side,
            int paddingSide, int paddingTopBottom, int elementDistance,
            GetterSetter<Integer> elementWidth
    ) {
        this.elements = elements;
        this.side = side;
        this.paddingSide = paddingSide;
        this.paddingTopBottom = paddingTopBottom;
        this.elementDistance = elementDistance;
        this.verticalSlider = 0;
        this.widthChangingState = false;

        this.elementWidth = elementWidth;
    }



    @Override
    public void initGui() {
        super.initGui();
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
    }



    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        this.drawBackground();

        int translateX = getTranslateX();

        GlStateManager.pushAttrib();
        GlStateManager.translate(translateX, this.paddingTopBottom - verticalSlider, 0);
        int currentHeight = this.paddingTopBottom - verticalSlider;

        this.totalHeight = 0;
        for(GuiSidebarElement element : elements) {
            if(element == null) continue;
            element.drawScreen(mouseX - translateX, mouseY - currentHeight, partialTicks);
            GlStateManager.translate(0, element.getHeight() + this.elementDistance, 0);
            currentHeight += element.getHeight() + this.elementDistance;
            this.totalHeight += currentHeight;
        }
        this.totalHeight += elementDistance;

        GlStateManager.popAttrib();
    }



    private void drawBackground() {
        ScaledResolution scaled = this.getScaledResolution();

        if(this.side == SidebarSide.LEFT) {
            int right = elementWidth.get() + 2 * this.paddingSide;

            Gui.drawRect(
                    0, 0,
                    right, scaled.getScaledHeight(),
                    0x3F000000
            );

            Gui.drawRect(right - 1, 0, right + 1, scaled.getScaledHeight(), 0xFFFFFF);
        }
        else if(this.side == SidebarSide.RIGHT) {
            int left = scaled.getScaledWidth() - elementWidth.get() - 2 * this.paddingSide;

            Gui.drawRect(
                    left, 0,
                    scaled.getScaledWidth(), scaled.getScaledHeight(),
                    0x3F000000
            );

            Gui.drawRect(left - 1, 0, left + 1, scaled.getScaledHeight(), 0xFFFFFF);
        }
    }



    int pMouseX, pMouseY;

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        int sidebarWidth = elementWidth.get() + 2 * this.paddingSide;

        if(side == SidebarSide.LEFT) {
            if(mouseX >= sidebarWidth - 3 && mouseX <= sidebarWidth + 3) {
                widthChangingState = true;
            }
        }
        else if(side == SidebarSide.RIGHT) {
            sidebarWidth = this.getScaledResolution().getScaledWidth() - sidebarWidth;
            if(mouseX >= sidebarWidth - 3 && mouseX <= sidebarWidth + 3) {
                widthChangingState = true;
            }
        }

        if(!widthChangingState) {
            int mx = mouseX - this.getTranslateX();

            int currentHeight = this.paddingTopBottom - verticalSlider;
            for (GuiSidebarElement element : elements) {
                if (element == null) continue;
                element.mouseClicked(mx, mouseY - currentHeight, mouseButton);
                currentHeight += element.getHeight() + this.elementDistance;
            }
        }

        this.pMouseX = mouseX; this.pMouseY = mouseY;
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
        ScaledResolution scaled = this.getScaledResolution();

        this.verticalSlider -= Mouse.getEventDWheel();

        if(this.verticalSlider > this.totalHeight - scaled.getScaledHeight()) {
            this.verticalSlider = this.totalHeight - scaled.getScaledHeight();
        }
        if(this.verticalSlider < 0) this.verticalSlider = 0;
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
        for(GuiSidebarElement element : elements) {
            if(element == null) continue;
            element.keyTyped(key, keyCode);
        }
    }



    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if(widthChangingState) {
            int dMouseX = mouseX - pMouseX;
            elementWidth.set(elementWidth.get() - dMouseX);

            if(elementWidth.get() > 270) {
                elementWidth.set(270);
            }

            if(elementWidth.get() < 130) {
                elementWidth.set(130);
            }

            System.out.println(elementWidth);
            for(GuiSidebarElement element : elements) {
                if(element == null) continue;
                element.onWidthChange(elementWidth.get());
            }
        }
        else {
            for (GuiSidebarElement element : elements) {
                if (element == null) continue;
                element.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
            }
        }

        this.pMouseX = mouseX; this.pMouseY = mouseY;
    }



    private ScaledResolution getScaledResolution() {
        return new ScaledResolution(mc);
    }

}
