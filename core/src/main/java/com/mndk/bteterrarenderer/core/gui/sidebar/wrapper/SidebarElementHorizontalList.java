package com.mndk.bteterrarenderer.core.gui.sidebar.wrapper;

import com.google.common.collect.Lists;
import com.mndk.bteterrarenderer.core.gui.sidebar.GuiSidebarElement;
import com.mndk.bteterrarenderer.mcconnector.client.MinecraftClientManager;
import com.mndk.bteterrarenderer.mcconnector.input.InputKey;
import com.mndk.bteterrarenderer.mcconnector.wrapper.DrawContextWrapper;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class SidebarElementHorizontalList extends GuiSidebarElement {

    private final List<Entry> entryList = new ArrayList<>();
    private final int sidePadding;
    private int elementsMaxPhysicalHeight, elementsMaxVisualHeight;
    private final boolean makeSound;

    public SidebarElementHorizontalList(int sidePadding, boolean makeSound) {
        this.sidePadding = sidePadding;
        this.makeSound = makeSound;
    }

    /**
     * @param widthFunction Set this to {@code null} to let horizontal list calculate the width
     */
    public SidebarElementHorizontalList add(GuiSidebarElement element, HListWidthFunction widthFunction) {
        if(element == null) return this;
        this.entryList.add(new Entry(element, widthFunction));
        return this;
    }

    @Override
    public int getPhysicalHeight() {
        return this.elementsMaxPhysicalHeight;
    }

    @Override
    public int getVisualHeight() {
        return this.elementsMaxVisualHeight;
    }

    @Override
    protected void init() {
        this.updateHorizontalDimensions();
        for(Entry entry : entryList) {
            GuiSidebarElement element = entry.element;
            if(element == null) continue;
            element.init(entry.width);
        }
    }

    @Override
    public void onWidthChange() {
        this.updateHorizontalDimensions();
        for(Entry entry : entryList) {
            GuiSidebarElement element = entry.element;
            if(element == null || element.hide) continue;
            element.onWidthChange(entry.width);
        }
        this.calculateHeights();
    }

    private void updateHorizontalDimensions() {
        int size = entryList.size(), totalWidth = this.getWidth();
        Double[] widths = new Double[size];

        // Calculate widths (double)
        double widthLeftForNulls = totalWidth - 2 * this.sidePadding;
        int nullFunctionCount = 0;
        for(int i = 0; i < size; i++) {
            Entry entry = entryList.get(i);
            GuiSidebarElement element = entry.element;
            if(element == null || element.hide) {
                widths[i] = 0.0;
                continue;
            }

            HListWidthFunction widthFunction = entry.widthFunction;
            if(widthFunction == null) {
                widths[i] = null;
                nullFunctionCount++;
                continue;
            }

            double width = Math.max(widthFunction.apply(totalWidth, widthLeftForNulls), 0);
            widths[i] = width;
            widthLeftForNulls -= width;
        }

        // Calculate xPos (double)
        double[] xPosList = new double[size+1];
        xPosList[0] = 0;
        for(int i = 0; i < size; i++) {
            double width;
            if(widths[i] == null) {
                width = nullFunctionCount != 0 ? Math.max(widthLeftForNulls / nullFunctionCount, 0) : 0;
            }
            else {
                width = widths[i];
            }
            xPosList[i+1] = xPosList[i] + width;
        }

        for(int i = 0; i < size; i++) {
            // Recalculate width (double -> int) and xPos (double -> int)
            Entry entry = entryList.get(i);
            entry.width = (int) xPosList[i+1] - (int) xPosList[i];
            entry.xPos = (int) xPosList[i];
        }
    }

    private void calculateHeights() {
        this.elementsMaxPhysicalHeight = 0;
        this.elementsMaxVisualHeight = 0;
        for(Entry entry : entryList) {
            GuiSidebarElement element = entry.element;

            // Calculate max height
            int physicalHeight = element.getPhysicalHeight();
            int visualHeight = element.getVisualHeight();
            if(physicalHeight > this.elementsMaxPhysicalHeight) this.elementsMaxPhysicalHeight = physicalHeight;
            if(visualHeight > this.elementsMaxVisualHeight) this.elementsMaxVisualHeight = visualHeight;
        }
    }

    @Override
    public void tick() {
        for(Entry entry : entryList) {
            GuiSidebarElement element = entry.element;
            if(element == null || element.hide) continue;
            element.tick();
        }
    }

    @Override
    public boolean mouseHovered(double mouseX, double mouseY, float partialTicks, boolean mouseHidden) {
        // Check for all elements
        boolean hovered = false;
        for(Entry entry : entryList) {
            GuiSidebarElement element = entry.element;
            if(element == null || element.hide) continue;

            boolean elementHovered = element.mouseHovered(
                    mouseX - this.sidePadding - entry.xPos, mouseY, partialTicks,
                    mouseHidden || hovered);
            if(elementHovered) hovered = true;
        }
        return hovered;
    }

    @Override
    public void drawComponent(DrawContextWrapper<?> drawContextWrapper) {
        int prevXPos = 0;

        drawContextWrapper.pushMatrix();
        drawContextWrapper.translate(this.sidePadding, 0, 0);
        // Draw from the last so that the first element could appear in the front
        for(Entry entry : Lists.reverse(entryList)) {
            GuiSidebarElement element = entry.element;
            if(element == null || element.hide || entry.width == 0) continue;

            int xPos = entry.xPos;
            drawContextWrapper.translate(xPos - prevXPos, 0, 0);
            prevXPos = xPos;

            element.drawComponent(drawContextWrapper);
            drawContextWrapper.translate(0, 0, element.getCount());
        }
        drawContextWrapper.popMatrix();
    }

    @Override
    public boolean mousePressed(double mouseX, double mouseY, int mouseButton) {
        // Check for all elements
        for(Entry entry : entryList) {
            GuiSidebarElement element = entry.element;
            if (element == null || element.hide) continue;

            int xPos = entry.xPos;
            boolean elementPressed = element.mousePressed(
                    mouseX - this.sidePadding - xPos, mouseY, mouseButton);
            if(elementPressed) {
                if(this.makeSound) MinecraftClientManager.playClickSound();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
        for(Entry entry : entryList) {
            GuiSidebarElement element = entry.element;
            if(element == null || element.hide) continue;

            int xPos = entry.xPos;
            element.mouseReleased(
                    mouseX - this.sidePadding - xPos, mouseY, mouseButton);
        }
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollAmount) {
        // Check for all elements
        for(Entry entry : entryList) {
            GuiSidebarElement element = entry.element;
            if(element == null || element.hide) continue;

            boolean elementScrolled = element.mouseScrolled(
                    mouseX - this.sidePadding - entry.xPos, mouseY, scrollAmount);
            if(elementScrolled) return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double pMouseX, double pMouseY) {
        // Check for every element
        for(Entry entry : entryList) {
            GuiSidebarElement element = entry.element;
            if (element == null || element.hide) continue;

            int xPos = entry.xPos;
            boolean elementDragged = element.mouseDragged(
                    mouseX - this.sidePadding - xPos, mouseY, mouseButton,
                    pMouseX - this.sidePadding - xPos, pMouseY);
            if(elementDragged) return true;
        }
        return false;
    }

    @Override
    public boolean keyTyped(char typedChar, int keyCode) {
        for(Entry entry : entryList) {
            GuiSidebarElement element = entry.element;
            if (element == null || element.hide) continue;
            if (element.keyTyped(typedChar, keyCode)) return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(InputKey key) {
        for(Entry entry : entryList) {
            GuiSidebarElement element = entry.element;
            if (element == null || element.hide) continue;
            if (element.keyPressed(key)) return true;
        }
        return false;
    }

    @Override
    public int getCount() {
        int count = 0;
        for(Entry entry : entryList) {
            GuiSidebarElement element = entry.element;
            if(element == null || element.hide) continue;
            count += element.getCount();
        }
        return count;
    }

    @RequiredArgsConstructor
    private static class Entry {
        final GuiSidebarElement element;
        @Nullable final HListWidthFunction widthFunction;
        int xPos = 0, width = 0;
    }
}
