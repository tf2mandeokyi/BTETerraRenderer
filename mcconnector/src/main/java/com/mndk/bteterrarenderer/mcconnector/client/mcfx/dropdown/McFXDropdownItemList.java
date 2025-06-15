package com.mndk.bteterrarenderer.mcconnector.client.mcfx.dropdown;

import com.mndk.bteterrarenderer.mcconnector.client.gui.GuiDrawContextWrapper;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import static com.mndk.bteterrarenderer.mcconnector.client.mcfx.dropdown.McFXDropdown.*;

@RequiredArgsConstructor
class McFXDropdownItemList extends McFXDropdownItem {
    boolean opened = false;
    final McFXDropdown parent;
    final String name;
    final boolean main;
    final List<McFXDropdownItem> itemList = new ArrayList<>();

    McFXDropdownItemList findCategory(String categoryName) {
        for (McFXDropdownItem item : this.itemList) {
            if (item == null) continue;
            if (!(item instanceof McFXDropdownItemList)) continue;
            McFXDropdownItemList category = (McFXDropdownItemList) item;
            if (category.name.equals(categoryName)) return category;
        }
        return null;
    }

    void toggleOpened() {
        this.opened = !this.opened;
    }

    int getCategoryHeight() {
        return this.main ? DROPDOWN_PADDING_TOP : parent.getSingleLineElementHeight() + ITEM_CATEGORY_PADDING_TOP;
    }

    @Override
    int calculateHeight(@Nonnull Stack<String> categoryPath) {
        categoryPath.push(this.name);
        int result = this.getCategoryHeight();
        if (this.opened) {
            for (McFXDropdownItem item : this.itemList) {
                result += item.calculateHeight(categoryPath);
            }
        }
        categoryPath.pop();
        return this.height = result;
    }

    @Override
    boolean checkMouseHovered(@Nonnull Stack<String> categoryPath, double mouseX, double mouseY) {
        categoryPath.push(this.name);
        int yOffset = this.getCategoryHeight();
        boolean result = this.mouseHovered = (!this.main && parent.mouseInHeight(mouseX, mouseY, yOffset));

        if (this.opened) for (McFXDropdownItem item : this.itemList) {
            if (result) { item.mouseIsNotHovered(); continue; }
            if (item.checkMouseHovered(categoryPath, mouseX, mouseY - yOffset)) result = true;
            yOffset += item.height;
        }
        categoryPath.pop();
        return result;
    }

    @Override
    void mouseIsNotHovered() {
        this.mouseHovered = false;
        if (this.opened) this.itemList.forEach(McFXDropdownItem::mouseIsNotHovered);
    }

    @Override
    void drawItem(
            GuiDrawContextWrapper drawContextWrapper,
            @Nonnull Stack<String> categoryPath,
            @Nullable String[] selectedCategoryPath,
            int selectedDepth, boolean isLast
    ) {
        int categoryColor = this.mouseHovered ? HOVERED_COLOR : NORMAL_TEXT_COLOR;

        if (!this.main) {
            // Category name
            drawContextWrapper.drawCenteredTextWithShadow(parent.getDefaultFont(),
                    this.name, parent.getWidth() / 2.0f, ITEM_PADDING_VERTICAL + ITEM_CATEGORY_PADDING_TOP, categoryColor);
            // Dropdown arrow
            parent.drawDropdownArrow(drawContextWrapper, ITEM_PADDING_VERTICAL + ITEM_CATEGORY_PADDING_TOP, categoryColor, this.opened);
        }
        drawContextWrapper.translate(0, this.getCategoryHeight(), 0);

        if (this.opened) {
            // Draw children
            this.drawChildren(drawContextWrapper, categoryPath, selectedCategoryPath, selectedDepth);
        }

        if (!isLast) {
            // Category separator line
            drawContextWrapper.fillRect(0, 0, parent.getWidth(), 1, ITEMLIST_SEPARATOR_LINE_COLOR);
        }
    }

    private void drawChildren(
            GuiDrawContextWrapper drawContextWrapper,
            @Nonnull Stack<String> categoryPath,
            @Nullable String[] selectedCategoryPath,
            int selectedDepth
    ) {
        categoryPath.push(this.name);
        if (selectedDepth != -1
                && selectedCategoryPath != null
                && selectedCategoryPath.length > selectedDepth
                && selectedCategoryPath[selectedDepth].equals(this.name)) {
            selectedDepth++; // Increase depth for children
        } else {
            selectedDepth = -1;
        }
        for (McFXDropdownItem item : this.itemList) {
            item.drawItem(drawContextWrapper, categoryPath, selectedCategoryPath, selectedDepth, false);
        }
        categoryPath.pop();
    }

    @Override
    void mouseClicked(@NotNull Stack<String> categoryPath) {
        if (this.mouseHovered) { this.toggleOpened(); return; }
        categoryPath.push(this.name);
        this.itemList.forEach(dropdownItem -> dropdownItem.mouseClicked(categoryPath));
        categoryPath.pop();
    }
}