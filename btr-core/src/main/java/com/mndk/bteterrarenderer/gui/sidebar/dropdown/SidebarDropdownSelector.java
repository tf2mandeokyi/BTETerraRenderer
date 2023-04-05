package com.mndk.bteterrarenderer.gui.sidebar.dropdown;

import com.mndk.bteterrarenderer.connector.minecraft.graphics.BufferBuilderConnector;
import com.mndk.bteterrarenderer.connector.minecraft.graphics.GlFactor;
import com.mndk.bteterrarenderer.connector.minecraft.graphics.GraphicsConnector;
import com.mndk.bteterrarenderer.connector.minecraft.graphics.VertexFormatConnectorEnum;
import com.mndk.bteterrarenderer.connector.minecraft.gui.GuiStaticConnector;
import com.mndk.bteterrarenderer.gui.sidebar.GuiSidebarElement;
import com.mndk.bteterrarenderer.loader.CategoryMapData;
import com.mndk.bteterrarenderer.util.GetterSetter;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class SidebarDropdownSelector<T extends CategoryMapData.ICategoryMapProperty> extends GuiSidebarElement {


    private static final int HORIZONTAL_PADDING = 12;
    private static final int VERTICAL_PADDING = 7;

    private static final int ELEMENT_VERTICAL_MARGIN = 5;
    private static final int DROPDOWN_VERTICAL_PADDING = 8;


    private final GetterSetter<CategoryMapData<T>> currentCategories;
    private final Supplier<String> currentCategoryName;
    private final Supplier<String> currentItemId;
    private final ItemSetter itemSetter;
    private final Function<T, String> nameGetter;

    private boolean opened = false;

    private int closedHeight, singleLineElementHeight, width, innerWidth;


    public CategoryMapData<T> getCurrentCategories() {
        return this.currentCategories.get();
    }


    @Override
    protected void init() {
        this.closedHeight = fontRenderer.getFontHeight() + VERTICAL_PADDING * 2;
        this.singleLineElementHeight = fontRenderer.getFontHeight() + ELEMENT_VERTICAL_MARGIN * 2;
        this.width = parent.elementWidth.get();
        this.innerWidth = width - HORIZONTAL_PADDING * 2;
    }


    @Override
    public int getHeight() {
        if(!opened) return closedHeight;
        int dy = 0;
        for(Map.Entry<String, DropdownCategory<T>> entry : currentCategories.get().getCategoryMap().entrySet()) {
            dy += singleLineElementHeight;

            DropdownCategory<T> category = entry.getValue();

            if(category.isOpened()) {
                for(T item : category.values()) {
                    dy += fontRenderer.getWordWrappedHeight(nameGetter.apply(item), innerWidth)
                            + ELEMENT_VERTICAL_MARGIN * 2;
                }
            }
        }
        return this.closedHeight + dy + DROPDOWN_VERTICAL_PADDING;
    }


    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {

        CategoryMapData<T> categories = currentCategories.get();
        Map<String, DropdownCategory<T>> categoryMap = categories.getCategoryMap();
        int rectColor = mouseInBox(mouseX, mouseY) ? 0xFFFFFFA0 : 0xFFFFFFFF;

        // Background
        GuiStaticConnector.INSTANCE.drawRect(0, 0, width, closedHeight, 0x80000000);
        if(opened) {
            GuiStaticConnector.INSTANCE.drawRect(0, closedHeight, width, getHeight(), 0xA0000000);
        }

        // Dropdown arrow
        this.drawDropdownArrow(VERTICAL_PADDING, rectColor, opened);

        // White Border
        GuiStaticConnector.INSTANCE.drawRect(-1, -1, 0, closedHeight + 1, rectColor);
        GuiStaticConnector.INSTANCE.drawRect(-1, -1, width, 0, rectColor);
        GuiStaticConnector.INSTANCE.drawRect(width, -1, width + 1, closedHeight + 1, rectColor);
        GuiStaticConnector.INSTANCE.drawRect(-1, closedHeight, width + 1, closedHeight + 1, rectColor);

        // Current selection
        T currentValue = categories.getItem(currentCategoryName.get(), currentItemId.get());

        if(currentValue != null) {
            String currentName = nameGetter.apply(currentValue).replace("\n", " ");
            int limit = innerWidth - fontRenderer.getFontHeight();
            // Handle overflow
            if(fontRenderer.getStringWidth(currentName) > limit) {
                currentName = fontRenderer.trimStringToWidth(currentName, limit);
            }
            fontRenderer.drawStringWithShadow(currentName, HORIZONTAL_PADDING, VERTICAL_PADDING, rectColor);
        }

        // Dropdown
        if(opened) {
            int totalHeight = 0;
            int yStart = closedHeight + DROPDOWN_VERTICAL_PADDING;

            int j = 0;
            for(Map.Entry<String, DropdownCategory<T>> categoryEntry : categoryMap.entrySet()) {
                String categoryName = categoryEntry.getKey();
                if(categoryName == null) continue;

                DropdownCategory<T> category = categoryEntry.getValue();
                int categoryColor = isMouseOnIndex(mouseX, mouseY, totalHeight, totalHeight + singleLineElementHeight)
                        ? 0xFFFFFFA0 : 0xFFFFFFFF;

                // Category name
                fontRenderer.drawCenteredStringWithShadow(
                        categoryName,
                        width / 2, yStart + totalHeight + ELEMENT_VERTICAL_MARGIN,
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
                    for (Map.Entry<String, T> itemEntry : category.entrySet()) {
                        String itemId = itemEntry.getKey();
                        if(itemId == null) continue;

                        T item = itemEntry.getValue();
                        String name = nameGetter.apply(item);

                        // Handle overflow
                        int height = fontRenderer.getWordWrappedHeight(nameGetter.apply(item), innerWidth)
                                + ELEMENT_VERTICAL_MARGIN * 2;
                        int color = isMouseOnIndex(mouseX, mouseY, totalHeight, totalHeight + height) 
                                ? 0xFFFFFFA0 : 0xFFFFFFFF;

                        // Blue background
                        if (categoryName.equals(currentCategoryName.get()) && itemId.equals(currentItemId.get())) {
                            GuiStaticConnector.INSTANCE.drawRect(
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
                if(j != categoryMap.size() - 1) {
                    // horizontal line
                    GuiStaticConnector.INSTANCE.drawRect(
                            0, yStart + totalHeight,
                            width, yStart + totalHeight + 1,
                            0xA0FFFFFF
                    );
                }
                j++;
            }


        }
    }


    private void drawDropdownArrow(int top, int colorARGB, boolean flip) {

        float alpha = (float)(colorARGB >> 24 & 255) / 255.0F;
        float red = (float)(colorARGB >> 16 & 255) / 255.0F;
        float green = (float)(colorARGB >> 8 & 255) / 255.0F;
        float blue = (float)(colorARGB & 255) / 255.0F;

        int bottom = top + fontRenderer.getFontHeight();
        int right = width - HORIZONTAL_PADDING;
        int left = width - HORIZONTAL_PADDING - fontRenderer.getFontHeight();

        if(flip) {
            int temp = top; top = bottom; bottom = temp;
            temp = right; right = left; left = temp;
        }

        BufferBuilderConnector builder = GraphicsConnector.INSTANCE.getBufferBuilder();
        GraphicsConnector.INSTANCE.glEnableBlend();
        GraphicsConnector.INSTANCE.glDisableTexture2D();
        GraphicsConnector.INSTANCE.glTryBlendFuncSeparate(GlFactor.SRC_ALPHA, GlFactor.ONE_MINUS_SRC_ALPHA, GlFactor.ONE, GlFactor.ZERO);
        GraphicsConnector.INSTANCE.glColor(red, green, blue, alpha);

        builder.begin(7, VertexFormatConnectorEnum.POSITION);
        builder.pos(left, top, 0.0D).endVertex();
        builder.pos((left + right) / 2., bottom, 0.0D).endVertex();
        builder.pos((left + right) / 2., bottom, 0.0D).endVertex();
        builder.pos(right, top, 0.0D).endVertex();
        GraphicsConnector.INSTANCE.tessellatorDraw();

        GraphicsConnector.INSTANCE.glEnableTexture2D();
        GraphicsConnector.INSTANCE.glDisableBlend();
    }


    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if(mouseInBox(mouseX, mouseY)) {
            opened = !opened;
            return true;
        }
        else {
            CategoryMapData<T> categories = currentCategories.get();
            Map<String, DropdownCategory<T>> categoryMap = categories.getCategoryMap();

            int totalHeight = 0;
            for(Map.Entry<String, DropdownCategory<T>> categoryEntry : categoryMap.entrySet()) {
                String categoryName = categoryEntry.getKey();
                DropdownCategory<T> category = categoryEntry.getValue();
                if(isMouseOnIndex(mouseX, mouseY, totalHeight, totalHeight + singleLineElementHeight)) {
                    category.setOpened(!category.isOpened());
                    return true;
                }
                totalHeight += singleLineElementHeight;
                if(category.isOpened()) {
                    for(Map.Entry<String, T> itemEntry : category.entrySet()) {
                        String itemId = itemEntry.getKey();
                        T item = itemEntry.getValue();
                        int height = fontRenderer.getWordWrappedHeight(nameGetter.apply(item), innerWidth)
                                + ELEMENT_VERTICAL_MARGIN * 2;
                        if(isMouseOnIndex(mouseX, mouseY, totalHeight, totalHeight + height)) {
                            itemSetter.set(categoryName, itemId);
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


    public interface ItemSetter {
        void set(String categoryName, String itemId);
    }
}
