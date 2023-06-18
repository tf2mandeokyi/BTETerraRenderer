package com.mndk.bteterrarenderer.gui.sidebar.dropdown;

import com.mndk.bteterrarenderer.connector.gui.FontConnector;
import com.mndk.bteterrarenderer.connector.gui.GuiStaticConnector;
import com.mndk.bteterrarenderer.graphics.GraphicsQuad;
import com.mndk.bteterrarenderer.gui.sidebar.GuiSidebarElement;
import com.mndk.bteterrarenderer.loader.CategoryMapData;
import com.mndk.bteterrarenderer.util.PropertyAccessor;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

// TODO: Fix category being closed after hiding the UI
@RequiredArgsConstructor
public class SidebarCategoryDropdownSelector<T extends CategoryMapData.ICategoryMapProperty> extends GuiSidebarElement {


    private static final int HORIZONTAL_PADDING = 12;
    private static final int VERTICAL_PADDING = 7;

    private static final int ELEMENT_VERTICAL_MARGIN = 5;
    private static final int DROPDOWN_VERTICAL_PADDING = 8;

    private static final int MAIN_RECT_BACKGROUND_COLOR = 0x80000000;
    private static final int MAIN_RECT_NORMAL_BORDER_COLOR = 0xFFFFFFFF;

    private static final int DROPDOWN_BACKGROUND_COLOR = 0xE8080808;
    private static final int SELECTED_BACKGROUND_COLOR = 0xDFA0AFFF;
    private static final int CATEGORY_SEPARATOR_LINE_COLOR = 0xA0FFFFFF;


    private final PropertyAccessor<CategoryMapData<T>> currentCategories;
    private final Supplier<String> currentCategoryName;
    private final Supplier<String> currentItemId;
    private final ItemSetter itemSetter;
    private final Function<T, String> nameGetter;

    private boolean opened = false, mouseOnMainBox = false;

    private int closedHeight, singleLineElementHeight, width, innerWidth, mouseHoverIndex;


    public CategoryMapData<T> getCurrentCategories() {
        return this.currentCategories.get();
    }


    @Override
    protected void init() {
        this.closedHeight = FontConnector.INSTANCE.getFontHeight() + VERTICAL_PADDING * 2;
        this.singleLineElementHeight = FontConnector.INSTANCE.getFontHeight() + ELEMENT_VERTICAL_MARGIN * 2;
        this.width = parent.elementWidth.get().intValue();
        this.innerWidth = width - HORIZONTAL_PADDING * 2;
    }


    @Override
    public int getPhysicalHeight() {
        return this.closedHeight;
    }


    @Override
    public int getVisualHeight() {
        if(!opened) return closedHeight;
        int totalElementsHeight = 0;
        for(Map.Entry<String, DropdownCategory<T>> entry : currentCategories.get().getCategoryMap().entrySet()) {
            totalElementsHeight += singleLineElementHeight;

            DropdownCategory<T> category = entry.getValue();

            if(category.isOpened()) {
                for(T item : category.values()) {
                    totalElementsHeight += FontConnector.INSTANCE.getWordWrappedHeight(nameGetter.apply(item), innerWidth)
                            + ELEMENT_VERTICAL_MARGIN * 2;
                }
            }
        }
        return this.closedHeight + totalElementsHeight + DROPDOWN_VERTICAL_PADDING;
    }


    @Override
    public boolean mouseHovered(double mouseX, double mouseY, float partialTicks, boolean mouseHidden) {
        this.mouseOnMainBox = mouseInBox(mouseX, mouseY);

        boolean result = !mouseHidden && mouseX >= 0 && mouseX <= width && mouseY >= 0 && mouseY < this.getVisualHeight();
        if(!opened) return result;

        // Dropdown
        CategoryMapData<T> categories = currentCategories.get();
        Map<String, DropdownCategory<T>> categoryMap = categories.getCategoryMap();
        FontConnector fontRenderer = FontConnector.INSTANCE;

        int totalHeight = 0;
        int hoverIndex = 0;
        this.mouseHoverIndex = -1;
        categoryLoop: for(Map.Entry<String, DropdownCategory<T>> categoryEntry : categoryMap.entrySet()) {
            String categoryName = categoryEntry.getKey();
            if(categoryName == null) continue;

            DropdownCategory<T> category = categoryEntry.getValue();
            if(isMouseOnIndex(mouseX, mouseY, totalHeight, totalHeight + singleLineElementHeight)) {
                this.mouseHoverIndex = hoverIndex;
                break;
            }

            hoverIndex++;
            totalHeight += singleLineElementHeight;

            // Items
            if(!category.isOpened()) continue;
            for (Map.Entry<String, T> itemEntry : category.entrySet()) {
                String itemId = itemEntry.getKey();
                if(itemId == null) continue;

                T item = itemEntry.getValue();

                // Handle overflow
                int height = fontRenderer.getWordWrappedHeight(nameGetter.apply(item), innerWidth)
                        + ELEMENT_VERTICAL_MARGIN * 2;
                if(isMouseOnIndex(mouseX, mouseY, totalHeight, totalHeight + height)) {
                    this.mouseHoverIndex = hoverIndex;
                    break categoryLoop;
                }

                hoverIndex++;
                totalHeight += height;
            }
        }

        return result;
    }


    @Override
    public void drawComponent(Object poseStack) {

        CategoryMapData<T> categories = currentCategories.get();
        Map<String, DropdownCategory<T>> categoryMap = categories.getCategoryMap();
        int rectColor = this.mouseOnMainBox ? HOVERED_COLOR : MAIN_RECT_NORMAL_BORDER_COLOR;
        FontConnector fontRenderer = FontConnector.INSTANCE;

        // Background
        GuiStaticConnector.INSTANCE.fillRect(poseStack, 0, 0, width, closedHeight, MAIN_RECT_BACKGROUND_COLOR);
        if(opened) {
            GuiStaticConnector.INSTANCE.fillRect(poseStack, 0, closedHeight, width, getVisualHeight(),
                    DROPDOWN_BACKGROUND_COLOR);
        }

        // Dropdown arrow
        this.drawDropdownArrow(poseStack, VERTICAL_PADDING, rectColor, opened);

        // White Border
        GuiStaticConnector.INSTANCE.fillRect(poseStack, -1, -1, 0, closedHeight + 1, rectColor);
        GuiStaticConnector.INSTANCE.fillRect(poseStack, -1, -1, width, 0, rectColor);
        GuiStaticConnector.INSTANCE.fillRect(poseStack, width, -1, width + 1, closedHeight + 1, rectColor);
        GuiStaticConnector.INSTANCE.fillRect(poseStack, -1, closedHeight, width + 1, closedHeight + 1, rectColor);

        // Current selection
        T currentValue = categories.getItem(currentCategoryName.get(), currentItemId.get());

        if(currentValue != null) {
            String currentName = nameGetter.apply(currentValue).replace("\n", " ");
            int limit = innerWidth - fontRenderer.getFontHeight();
            // Handle overflow
            if(fontRenderer.getStringWidth(currentName) > limit) {
                currentName = fontRenderer.trimStringToWidth(currentName, limit);
            }
            fontRenderer.drawStringWithShadow(poseStack, currentName, HORIZONTAL_PADDING, VERTICAL_PADDING, rectColor);
        }

        if (!opened) return;

        // Dropdown
        int totalHeight = 0, yStart = closedHeight + DROPDOWN_VERTICAL_PADDING;
        int hoverIndex = 0, categoryIndex = 0;
        for(Map.Entry<String, DropdownCategory<T>> categoryEntry : categoryMap.entrySet()) {
            String categoryName = categoryEntry.getKey();
            if(categoryName == null) continue;

            DropdownCategory<T> category = categoryEntry.getValue();
            int categoryColor = mouseHoverIndex == (hoverIndex++) ? HOVERED_COLOR : NORMAL_TEXT_COLOR;

            // Category name
            fontRenderer.drawCenteredStringWithShadow(poseStack,
                    categoryName,
                    width / 2.0f, yStart + totalHeight + ELEMENT_VERTICAL_MARGIN,
                    categoryColor
            );

            // Dropdown arrow
            this.drawDropdownArrow(poseStack,
                    yStart + totalHeight + ELEMENT_VERTICAL_MARGIN,
                    categoryColor,
                    category.isOpened()
            );
            totalHeight += singleLineElementHeight;

            if(category.isOpened()) {
                // Items
                for (Map.Entry<String, T> itemEntry : category.entrySet()) {
                    String itemId = itemEntry.getKey();
                    if (itemId == null) continue;

                    T item = itemEntry.getValue();
                    String name = nameGetter.apply(item);

                    // Handle overflow
                    int height = fontRenderer.getWordWrappedHeight(nameGetter.apply(item), innerWidth)
                            + ELEMENT_VERTICAL_MARGIN * 2;
                    int color = mouseHoverIndex == (hoverIndex++) ? HOVERED_COLOR : NORMAL_TEXT_COLOR;

                    // Selected background
                    if (categoryName.equals(currentCategoryName.get()) && itemId.equals(currentItemId.get())) {
                        GuiStaticConnector.INSTANCE.fillRect(poseStack,
                                0, yStart + totalHeight,
                                width, yStart + totalHeight + height,
                                SELECTED_BACKGROUND_COLOR
                        );
                    }

                    // Item text
                    fontRenderer.drawSplitString(poseStack,
                            name, HORIZONTAL_PADDING, yStart + totalHeight + ELEMENT_VERTICAL_MARGIN,
                            innerWidth, color
                    );
                    totalHeight += height;
                }
            }

            // Category separator line
            if(categoryIndex != categoryMap.size() - 1) {
                // horizontal line
                GuiStaticConnector.INSTANCE.fillRect(poseStack,
                        0, yStart + totalHeight,
                        width, yStart + totalHeight + 1,
                        CATEGORY_SEPARATOR_LINE_COLOR
                );
            }
            categoryIndex++;
        }
    }


    private void drawDropdownArrow(Object poseStack, int top, int colorARGB, boolean flip) {
        int bottom = top + FontConnector.INSTANCE.getFontHeight();
        int right = width - HORIZONTAL_PADDING;
        int left = width - HORIZONTAL_PADDING - FontConnector.INSTANCE.getFontHeight();

        if (flip) {
            int temp = top;
            top = bottom;
            bottom = temp;
            temp = right;
            right = left;
            left = temp;
        }

        GraphicsQuad<GraphicsQuad.Pos> quad = new GraphicsQuad<>(
                new GraphicsQuad.Pos(left, top, 0),
                new GraphicsQuad.Pos((left + right) / 2f, bottom, 0),
                new GraphicsQuad.Pos((left + right) / 2f, bottom, 0),
                new GraphicsQuad.Pos(right, top, 0)
        );
        GuiStaticConnector.INSTANCE.fillQuad(poseStack, quad, colorARGB);
    }


    @Override
    public boolean mousePressed(double mouseX, double mouseY, int mouseButton) {
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
                        int height = FontConnector.INSTANCE.getWordWrappedHeight(nameGetter.apply(item), innerWidth)
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


    private boolean mouseInBox(double mouseX, double mouseY) {
        return mouseX >= 0 && mouseX <= width && mouseY >= 0 && mouseY <= closedHeight;
    }


    private boolean isMouseOnIndex(double mouseX, double mouseY, int yMin, int yMax) {
        if(!opened) return false;

        int yStart = closedHeight + DROPDOWN_VERTICAL_PADDING;

        return mouseX >= 0 && mouseX <= width &&
                mouseY >= yStart + yMin && mouseY < yStart + yMax;
    }


    @Override public void onWidthChange(double newWidth) {
        this.width = (int) newWidth;
        this.innerWidth = (int) (newWidth - HORIZONTAL_PADDING * 2);
    }


    public interface ItemSetter {
        void set(String categoryName, String itemId);
    }
}
