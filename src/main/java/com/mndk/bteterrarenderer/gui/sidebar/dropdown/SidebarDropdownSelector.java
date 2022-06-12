package com.mndk.bteterrarenderer.gui.sidebar.dropdown;

import com.mndk.bteterrarenderer.gui.sidebar.GuiSidebarElement;
import com.mndk.bteterrarenderer.util.GetterSetter;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

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

    private int closedHeight, singleLineElementHeight, width, innerWidth;


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
        this.closedHeight = fontRenderer.FONT_HEIGHT + VERTICAL_PADDING * 2;
        this.singleLineElementHeight = fontRenderer.FONT_HEIGHT + ELEMENT_VERTICAL_MARGIN * 2;
        this.width = parent.elementWidth.get();
        this.innerWidth = width - HORIZONTAL_PADDING * 2;
    }


    @Override
    public int getHeight() {
        if(!opened) return closedHeight;
        int dy = 0;
        for(SidebarDropdownCategory<T> category : categories) {
            dy += singleLineElementHeight;
            if(category.isOpened()) {
                for(T item : category.getItems()) {
                    dy += fontRenderer.getWordWrappedHeight(nameGetter.apply(item), innerWidth)
                            + ELEMENT_VERTICAL_MARGIN * 2;
                }
            }
        }
        return this.closedHeight + dy + DROPDOWN_VERTICAL_PADDING;
    }


    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {

        int rectColor = mouseInBox(mouseX, mouseY) ? 0xFFFFFFA0 : 0xFFFFFFFF;

        // Background
        Gui.drawRect(0, 0, width, closedHeight, 0x80000000);
        if(opened) {
            Gui.drawRect(0, closedHeight, width, getHeight(), 0xA0000000);
        }

        // Dropdown arrow
        this.drawDropdownArrow(VERTICAL_PADDING, rectColor, opened);

        // White Border
        Gui.drawRect(-1, -1, 0, closedHeight + 1, rectColor);
        Gui.drawRect(-1, -1, width, 0, rectColor);
        Gui.drawRect(width, -1, width + 1, closedHeight + 1, rectColor);
        Gui.drawRect(-1, closedHeight, width + 1, closedHeight + 1, rectColor);

        // Current selection
        T currentValue = value.get();

        if(currentValue != null) {
            String currentName = nameGetter.apply(currentValue).replace("\n", " ");
            int limit = innerWidth - fontRenderer.FONT_HEIGHT;
            // Handle overflow
            if(fontRenderer.getStringWidth(currentName) > limit) {
                currentName = fontRenderer.trimStringToWidth(currentName, limit);
            }
            this.drawString(fontRenderer, currentName, HORIZONTAL_PADDING, VERTICAL_PADDING, rectColor);
        }

        // Dropdown
        if(opened) {
            int totalHeight = 0;
            int yStart = closedHeight + DROPDOWN_VERTICAL_PADDING;

            for(int j = 0; j < categories.size(); ++j) {
                SidebarDropdownCategory<T> category = categories.get(j);
                String categoryName = category.getName();
                int categoryColor = isMouseOnIndex(mouseX, mouseY, totalHeight, totalHeight + singleLineElementHeight) 
                        ? 0xFFFFFFA0 : 0xFFFFFFFF;

                // Category name
                this.drawCenteredString(fontRenderer, categoryName,
                        width / 2,
                        yStart + totalHeight + ELEMENT_VERTICAL_MARGIN,
                        categoryColor
                );

                // Dropdown arrow
                this.drawDropdownArrow(
                        yStart + totalHeight + ELEMENT_VERTICAL_MARGIN,
                        categoryColor,
                        category.isOpened()
                );
                totalHeight += singleLineElementHeight;

                // Items
                if(category.isOpened()) {
                    for (T item : category.getItems()) {
                        String name = nameGetter.apply(item);

                        // Handle overflow
                        int height = fontRenderer.getWordWrappedHeight(nameGetter.apply(item), innerWidth)
                                + ELEMENT_VERTICAL_MARGIN * 2;
                        int color = isMouseOnIndex(mouseX, mouseY, totalHeight, totalHeight + height) 
                                ? 0xFFFFA0 : 0xFFFFFF;

                        // Blue background
                        if (item.equals(currentValue)) {
                            Gui.drawRect(
                                    0, yStart + totalHeight,
                                    width, yStart + totalHeight + height,
                                    0xDFA0AFFF
                            );
                        }

                        // Item text
                        fontRenderer.drawSplitString(
                                name, HORIZONTAL_PADDING, yStart + totalHeight + ELEMENT_VERTICAL_MARGIN,
                                innerWidth, color
                        );
                        totalHeight += height;
                    }
                }
                if(j != categories.size() - 1) {
                    // horizontal line
                    Gui.drawRect(
                            0, yStart + totalHeight,
                            width, yStart + totalHeight + 1,
                            0xA0FFFFFF
                    );
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
        int right = width - HORIZONTAL_PADDING;
        int left = width - HORIZONTAL_PADDING - fontRenderer.FONT_HEIGHT;

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
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if(mouseInBox(mouseX, mouseY)) {
            opened = !opened;
            return true;
        }
        else {
            int totalHeight = 0;
            for(SidebarDropdownCategory<T> category : categories) {
                if(isMouseOnIndex(mouseX, mouseY, totalHeight, totalHeight + singleLineElementHeight)) {
                    category.setOpened(!category.isOpened());
                    return true;
                }
                totalHeight += singleLineElementHeight;
                if(category.isOpened()) {
                    for(T item : category.getItems()) {
                        int height = fontRenderer.getWordWrappedHeight(nameGetter.apply(item), innerWidth)
                                + ELEMENT_VERTICAL_MARGIN * 2;
                        if(isMouseOnIndex(mouseX, mouseY, totalHeight, totalHeight + height)) {
                            value.set(item);
                            return true;
                        }
                        totalHeight += height;
                    }
                }
            }
        }
        return false;
    }


    private boolean mouseInBox(int mouseX, int mouseY) {
        return mouseX >= 0 && mouseX <= width && mouseY >= 0 && mouseY <= closedHeight;
    }


    private boolean isMouseOnIndex(int mouseX, int mouseY, int yMin, int yMax) {
        if(!opened) return false;

        int yStart = closedHeight + DROPDOWN_VERTICAL_PADDING;

        return mouseX >= 0 && mouseX <= width &&
                mouseY >= yStart + yMin && mouseY < yStart + yMax;
    }


    @Override public void onWidthChange(int newWidth) {
        this.width = newWidth;
        this.innerWidth = newWidth - HORIZONTAL_PADDING * 2;
    }


    @Override public void updateScreen() {}
    @Override public void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {}
    @Override public void mouseReleased(int mouseX, int mouseY, int state) {}
    @Override public boolean keyTyped(char key, int keyCode) { return false; }
}
