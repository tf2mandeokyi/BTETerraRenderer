package com.mndk.bteterrarenderer.core.gui.mcfx.list;

import com.google.common.collect.Lists;
import com.mndk.bteterrarenderer.core.gui.mcfx.McFXElement;
import com.mndk.bteterrarenderer.mcconnector.McConnector;
import com.mndk.bteterrarenderer.mcconnector.client.input.InputKey;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.DrawContextWrapper;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class McFXHorizontalList extends McFXElement {

    private final List<Entry> entryList = new ArrayList<>();
    private final int sidePadding;
    private int elementsMaxPhysicalHeight, elementsMaxVisualHeight;
    private final boolean makeSound;

    public McFXHorizontalList(int sidePadding, boolean makeSound) {
        this.sidePadding = sidePadding;
        this.makeSound = makeSound;
    }

    public McFXHorizontalList clear() {
        this.entryList.clear();
        return this;
    }

    /**
     * @param widthFunction Set this to {@code null} to let horizontal list calculate the width
     */
    public McFXHorizontalList add(McFXElement newElement, WidthFunction widthFunction) {
        if(newElement == null) return this;

        int index = this.entryList.size();
        this.entryList.add(new Entry(newElement, widthFunction));

        this.updateHorizontalDimensions();
        for(int i = 0; i < entryList.size(); i++) {
            Entry entry = entryList.get(i);
            McFXElement element = entry.element;
            if(element == null) continue;

            if(i == index) element.init(entry.width);
            else if(entry.widthChanged) {
                element.onWidthChange(entry.width);
                entry.widthChanged = false;
            }
        }

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
            McFXElement element = entry.element;
            if(element == null) continue;
            element.init(entry.width);
        }
    }

    @Override
    public void onWidthChange() {
        this.updateHorizontalDimensions();
        for(Entry entry : entryList) {
            McFXElement element = entry.element;
            if(element == null || element.hide) continue;
            element.onWidthChange(entry.width);
            entry.widthChanged = false;
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
            McFXElement element = entry.element;
            if(element == null || element.hide) {
                widths[i] = 0.0;
                continue;
            }

            WidthFunction widthFunction = entry.widthFunction;
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
            entry.xPos = (int) xPosList[i];

            int newWidth = (int) xPosList[i+1] - (int) xPosList[i];
            if(entry.width != newWidth) {
                entry.widthChanged = true;
                entry.width = newWidth;
            }
        }
    }

    private void calculateHeights() {
        this.elementsMaxPhysicalHeight = 0;
        this.elementsMaxVisualHeight = 0;
        for(Entry entry : entryList) {
            McFXElement element = entry.element;

            // Calculate max height
            int physicalHeight = element.getPhysicalHeight();
            int visualHeight = element.getVisualHeight();
            if(physicalHeight > this.elementsMaxPhysicalHeight) this.elementsMaxPhysicalHeight = physicalHeight;
            if(visualHeight > this.elementsMaxVisualHeight) this.elementsMaxVisualHeight = visualHeight;
        }
    }

    @Override
    public void tick() {
        this.updateHorizontalDimensions();
        for(Entry entry : entryList) {
            McFXElement element = entry.element;
            if(element == null || element.hide) continue;
            element.tick();
            if(entry.widthChanged) {
                element.onWidthChange(entry.width);
                entry.widthChanged = false;
            }
        }
    }

    @Override
    public boolean mouseHovered(int mouseX, int mouseY, float partialTicks, boolean mouseHidden) {
        // Check for all elements
        boolean hovered = false;
        for(Entry entry : entryList) {
            McFXElement element = entry.element;
            if(element == null || element.hide) continue;

            boolean elementHovered = element.mouseHovered(
                    mouseX - this.sidePadding - entry.xPos, mouseY, partialTicks,
                    mouseHidden || hovered);
            if(elementHovered) hovered = true;
        }
        return hovered;
    }

    @Override
    public void drawElement(DrawContextWrapper<?> drawContextWrapper) {
        int prevXPos = 0;

        drawContextWrapper.pushMatrix();
        drawContextWrapper.translate(this.sidePadding, 0, 0);
        // Draw from the last so that the first element could appear in the front
        for(Entry entry : Lists.reverse(entryList)) {
            McFXElement element = entry.element;
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
            McFXElement element = entry.element;
            if (element == null || element.hide) continue;

            int xPos = entry.xPos;
            boolean elementPressed = element.mousePressed(
                    mouseX - this.sidePadding - xPos, mouseY, mouseButton);
            if(elementPressed) {
                if(this.makeSound) McConnector.client().playClickSound();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
        for(Entry entry : entryList) {
            McFXElement element = entry.element;
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
            McFXElement element = entry.element;
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
            McFXElement element = entry.element;
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
    public boolean charTyped(char typedChar, int keyCode) {
        for(Entry entry : entryList) {
            McFXElement element = entry.element;
            if (element == null || element.hide) continue;
            if (element.charTyped(typedChar, keyCode)) return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(InputKey key, int scanCode, int modifiers) {
        for(Entry entry : entryList) {
            McFXElement element = entry.element;
            if (element == null || element.hide) continue;
            if (element.keyPressed(key, scanCode, modifiers)) return true;
        }
        return false;
    }

    @Override
    public boolean handleScreenEscape() {
        for(Entry entry : entryList) {
            McFXElement element = entry.element;
            if (element == null || element.hide) continue;
            if (!element.handleScreenEscape()) return false;
        }
        return true;
    }

    @Override
    public int getCount() {
        int count = 0;
        for(Entry entry : entryList) {
            McFXElement element = entry.element;
            if(element == null || element.hide) continue;
            count += element.getCount();
        }
        return count;
    }

    @RequiredArgsConstructor
    private static class Entry {
        final McFXElement element;
        @Nullable final WidthFunction widthFunction;
        int xPos = 0, width = 0;
        boolean widthChanged = false;
    }
}
