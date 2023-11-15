package com.mndk.bteterrarenderer.core.gui.sidebar;

import com.google.common.collect.Lists;
import com.mndk.bteterrarenderer.core.graphics.GlGraphicsManager;
import com.mndk.bteterrarenderer.core.util.i18n.I18nManager;
import com.mndk.bteterrarenderer.core.input.InputKey;
import com.mndk.bteterrarenderer.core.util.minecraft.MinecraftClientManager;
import com.mndk.bteterrarenderer.core.gui.sidebar.input.SidebarNumberInput;
import com.mndk.bteterrarenderer.core.gui.sidebar.slider.SidebarSlider;
import com.mndk.bteterrarenderer.core.util.BTRUtil;
import com.mndk.bteterrarenderer.core.util.accessor.PropertyAccessor;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SidebarElementListComponent extends GuiSidebarElement {

    private final List<Entry> entryList = new ArrayList<>();
    private final int elementDistance;
    private int totalPhysicalHeight, totalVisualHeight;
    private final boolean makeSound;

    public SidebarElementListComponent(int elementDistance, boolean makeSound) {
        this.elementDistance = elementDistance;
        this.makeSound = makeSound;
    }

    public void set(List<GuiSidebarElement> elements) {
        this.entryList.clear();
        for(GuiSidebarElement element : elements) {
            this.entryList.add(new Entry(element));
        }
        if(this.parent != null) {
            this.initComponent(this.parent);
        }
    }

    public void setProperties(List<PropertyAccessor.Localized<?>> properties) {
        List<GuiSidebarElement> elements = properties.stream()
                .map(SidebarElementListComponent::makeElementFromProperty)
                .collect(Collectors.toList());
        this.set(elements);
    }

    @Override
    public int getPhysicalHeight() {
        return this.totalPhysicalHeight;
    }

    @Override
    public int getVisualHeight() {
        return this.totalVisualHeight;
    }

    @Override
    public void init() {
        for(Entry entry : entryList) {
            GuiSidebarElement element = entry.element;
            if(element == null) continue;
            element.initComponent(this.parent);
        }
        // This is to avoid the initial visual glitch
        this.calculatePositions();
    }

    private void calculatePositions() {
        this.totalPhysicalHeight = 0;
        this.totalVisualHeight = 0;

        for(Entry entry : entryList) {
            GuiSidebarElement element = entry.element;
            if(element == null || element.hide) continue;

            int physicalHeight = element.getPhysicalHeight();
            int visualHeight = element.getVisualHeight();

            if(visualHeight > 0) {
                int newTotalVisualHeight = this.totalPhysicalHeight + visualHeight;
                if (newTotalVisualHeight > this.totalVisualHeight) {
                    this.totalVisualHeight = newTotalVisualHeight;
                }
            }

            entry.yPos = this.totalPhysicalHeight;
            this.totalPhysicalHeight += physicalHeight + this.elementDistance;
        }
        this.totalPhysicalHeight -= this.elementDistance;
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
        this.calculatePositions();
        boolean hovered = false;
        for(Entry entry : entryList) {
            GuiSidebarElement element = entry.element;
            if(element == null || element.hide) continue;

            if(element.mouseHovered(mouseX, mouseY - entry.yPos, partialTicks, mouseHidden || hovered)) {
                hovered = true;
            }
        }
        return hovered;
    }

    @Override
    public void drawComponent(Object poseStack) {
        int prevYPos = 0;

        GlGraphicsManager.glPushMatrix(poseStack);
        for(Entry entry : Lists.reverse(entryList)) {
            GuiSidebarElement element = entry.element;
            if(element == null || element.hide) continue;

            int yPos = entry.yPos;
            GlGraphicsManager.glTranslate(poseStack, 0, yPos - prevYPos, 1);

            element.drawComponent(poseStack);
            prevYPos = yPos;
        }
        GlGraphicsManager.glPopMatrix(poseStack);
    }

    @Override
    public boolean mousePressed(double mouseX, double mouseY, int mouseButton) {
        for(Entry entry : entryList) {
            GuiSidebarElement element = entry.element;
            if (element == null || element.hide) continue;

            int yPos = entry.yPos;
            if(element.mousePressed(mouseX, mouseY - yPos, mouseButton)) {
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

            int yPos = entry.yPos;
            element.mouseReleased(mouseX, mouseY - yPos, mouseButton);
        }
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double pMouseX, double pMouseY) {
        for(Entry entry : entryList) {
            GuiSidebarElement element = entry.element;
            if (element == null || element.hide) continue;

            int yPos = entry.yPos;
            boolean result = element.mouseDragged(
                    mouseX, mouseY - yPos, mouseButton,
                    pMouseX, pMouseY - yPos);
            if(result) return true;
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
    public void onWidthChange(double newWidth) {
        for(Entry entry : entryList) {
            GuiSidebarElement element = entry.element;
            if(element == null || element.hide) continue;
            element.onWidthChange(newWidth);
        }
    }

    private static GuiSidebarElement makeElementFromProperty(PropertyAccessor.Localized<?> property) {
        Class<?> propertyClass = property.delegate.getPropertyClass();

        String localized = I18nManager.format(property.i18nKey);

        if(Number.class.isAssignableFrom(propertyClass)) {
            PropertyAccessor<Number> numberProperty = BTRUtil.uncheckedCast(property.delegate);
            if(numberProperty instanceof PropertyAccessor.Ranged) {
                PropertyAccessor.Ranged<Number> rangedNumberProperty = BTRUtil.uncheckedCast(numberProperty);
                return new SidebarSlider<>(rangedNumberProperty, localized + ": ", "");
            }

            PropertyAccessor<Double> propertyWrapper = PropertyAccessor.of(
                    () -> numberProperty.get().doubleValue(),
                    v  -> numberProperty.set(BTRUtil.doubleToNumber(numberProperty.getPropertyClass(), v))
            );
            return new SidebarNumberInput(propertyWrapper, localized);
        }
        throw new RuntimeException("Unsupported property type: " + propertyClass);
    }

    @RequiredArgsConstructor
    private static class Entry {
        final GuiSidebarElement element;
        int yPos = 0;
    }
}
