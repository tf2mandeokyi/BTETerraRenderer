package com.mndk.bteterrarenderer.gui.sidebar.elem;

import com.mndk.bteterrarenderer.gui.sidebar.GuiSidebarElement;
import com.mndk.bteterrarenderer.util.GetterSetter;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class SidebarDropdownSelector<T> extends GuiSidebarElement {


    private static final int HORIZONTAL_PADDING = 12;
    private static final int VERTICAL_PADDING = 7;

    private static final int ELEMENT_VERTICAL_MARGIN = 5;
    private static final int DROPDOWN_VERTICAL_PADDING = 8;


    private final GetterSetter<T> value;
    private final Function<T, String> nameGetter;
    private final List<SidebarDropdownCategory<T>> categories;
    private boolean opened = false;

    private int height, elementHeight;


    public SidebarDropdownSelector(GetterSetter<T> value, Function<T, String> nameGetter) {
        this.value = value;
        this.nameGetter = nameGetter;
        this.categories = new ArrayList<>();
    }


    public List<SidebarDropdownCategory<T>> getCategories() {
        return this.categories;
    }


    public void addCategory(SidebarDropdownCategory<T> category) {
        this.categories.add(category);
    }


    public void addCategories(Collection<? extends SidebarDropdownCategory<T>> categories) {
        this.categories.addAll(categories);
    }


    public void clearCategories() {
        this.categories.clear();
    }


    @Override
    protected void init() {
        this.height = fontRenderer.FONT_HEIGHT + VERTICAL_PADDING * 2;
        this.elementHeight = fontRenderer.FONT_HEIGHT + ELEMENT_VERTICAL_MARGIN * 2;
    }


    private int getShownElementLength() {
        int result = 0;
        for(SidebarDropdownCategory<T> category : categories) {
            result++;
            if(category.isOpened()) {
                result += category.getItems().size();
            }
        }
        return result;
    }


    @Override
    public int getHeight() {
        return opened ?
                this.height + (getShownElementLength() * this.elementHeight) + DROPDOWN_VERTICAL_PADDING :
                this.height;
    }


    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {

        int width = parent.elementWidth.get();
        int rectColor = mouseInBox(mouseX, mouseY) ? 0xFFFFFFA0 : 0xFFFFFFFF;

        // Background
        Gui.drawRect(0, 0, width, height, 0x50000000);
        if(opened) {
            Gui.drawRect(0, height, width, getHeight(), 0x80000000);
        }

        // Dropdown arrow
        this.drawDropdownArrow(VERTICAL_PADDING, rectColor, opened);

        // White Border
        Gui.drawRect(-1, -1, 0, height + 1, rectColor);
        Gui.drawRect(-1, -1, width, 0, rectColor);
        Gui.drawRect(width, -1, width + 1, height + 1, rectColor);
        Gui.drawRect(-1, height, width + 1, height + 1, rectColor);

        // Current selection
        T currentValue = value.get();
        if(currentValue != null) {
            this.drawString(fontRenderer, nameGetter.apply(currentValue), HORIZONTAL_PADDING, VERTICAL_PADDING, rectColor);
        }

        // Dropdown
        if(opened) {
            int i = 0;
            int yStart = height + DROPDOWN_VERTICAL_PADDING;

            for(SidebarDropdownCategory<T> category : categories) {
                String categoryName = category.getName();
                int categoryColor = isMouseOnIndex(mouseX, mouseY, i) ? 0xFFFFFFA0 : 0xFFFFFFFF;

                // Category name
                this.drawCenteredString(this.fontRenderer, categoryName,
                        width / 2,
                        yStart + elementHeight * i + ELEMENT_VERTICAL_MARGIN,
                        categoryColor
                );

                // Dropdown arrow
                this.drawDropdownArrow(
                        yStart + elementHeight * i + ELEMENT_VERTICAL_MARGIN,
                        categoryColor,
                        category.isOpened()
                );
                ++i;

                // Items
                if(category.isOpened()) {
                    for (T item : category.getItems()) {
                        String name = nameGetter.apply(item);
                        int color = isMouseOnIndex(mouseX, mouseY, i) ? 0xFFFFA0 : 0xFFFFFF;

                        // Blue background
                        if (item.equals(currentValue)) {
                            Gui.drawRect(
                                    0, yStart + elementHeight * i,
                                    width, yStart + elementHeight * (i + 1),
                                    0xDFA0AFFF
                            );
                        }

                        // Item text
                        this.drawString(
                                this.fontRenderer, name,
                                HORIZONTAL_PADDING, yStart + elementHeight * i + ELEMENT_VERTICAL_MARGIN,
                                color
                        );
                        ++i;
                    }
                }
            }
        }
    }


    private void drawDropdownArrow(int top, int colorARGB, boolean flip) {

        float alpha = (float)(colorARGB >> 24 & 255) / 255.0F;
        float red = (float)(colorARGB >> 16 & 255) / 255.0F;
        float green = (float)(colorARGB >> 8 & 255) / 255.0F;
        float blue = (float)(colorARGB & 255) / 255.0F;

        int bottom = top + fontRenderer.FONT_HEIGHT;
        int right = parent.elementWidth.get() - HORIZONTAL_PADDING;
        int left = parent.elementWidth.get() - HORIZONTAL_PADDING - fontRenderer.FONT_HEIGHT;

        if(flip) {
            int temp = top; top = bottom; bottom = temp;
            temp = right; right = left; left = temp;
        }

        Tessellator t = Tessellator.getInstance();
        BufferBuilder b = t.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.color(red, green, blue, alpha);

        b.begin(7, DefaultVertexFormats.POSITION);
        b.pos(left, top, 0.0D).endVertex();
        b.pos((left + right) / 2., bottom, 0.0D).endVertex();
        b.pos((left + right) / 2., bottom, 0.0D).endVertex();
        b.pos(right, top, 0.0D).endVertex();
        t.draw();

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }


    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if(mouseInBox(mouseX, mouseY)) {
            opened = !opened;
            return true;
        }
        else {
            int i = 0;
            for(SidebarDropdownCategory<T> category : categories) {
                if(isMouseOnIndex(mouseX, mouseY, i)) {
                    category.setOpened(!category.isOpened());
                    return true;
                }
                i++;
                if(category.isOpened()) {
                    for(T item : category.getItems()) {
                        if(isMouseOnIndex(mouseX, mouseY, i)) {
                            value.set(item);
                            return true;
                        }
                        ++i;
                    }
                }
            }
        }
        return false;
    }


    private boolean mouseInBox(int mouseX, int mouseY) {
        return mouseX >= 0 && mouseX <= parent.elementWidth.get() && mouseY >= 0 && mouseY <= height;
    }


    private boolean isMouseOnIndex(int mouseX, int mouseY, int index) {
        if(!opened) return false;

        int yStart = height + DROPDOWN_VERTICAL_PADDING;

        return mouseX >= 0 && mouseX <= parent.elementWidth.get() &&
                mouseY >= yStart + elementHeight * index && mouseY < yStart + elementHeight * (index + 1);
    }


/*
    private int getMouseIndex(int mouseX, int mouseY) { // TODO implement this you lazy
        if(mouseX < 0 || mouseX > parent.elementWidth.get()) return -1;

        int c = LIST_TOP_MARGIN + LIST_PADDING + this.fontRenderer.FONT_HEIGHT + TITLE_MARGIN_BOTTOM;
        int h = this.fontRenderer.FONT_HEIGHT + ELEMENT_TOP_MARGIN;
        int index = (int) Math.round((mouseY - c) / (double) h), y = c + h * index;

        if(index < 0 || index >= clickableElementList.size()) return -1;
        if(mouseY - y < -8 || mouseY - y > 8) return -1; // If the cursor is at the gap between elements
        return index;
    }
    */


    @Override public void onWidthChange(int newWidth) {}
    @Override public void updateScreen() {}
    @Override public void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {}
    @Override public void mouseReleased(int mouseX, int mouseY, int state) {}
    @Override public void keyTyped(char key, int keyCode) throws IOException {}
}
