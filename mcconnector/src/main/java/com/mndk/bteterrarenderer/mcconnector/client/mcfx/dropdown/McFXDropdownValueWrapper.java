package com.mndk.bteterrarenderer.mcconnector.client.mcfx.dropdown;

import com.mndk.bteterrarenderer.mcconnector.client.graphics.NativeTextureWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.gui.GuiDrawContextWrapper;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Stack;

import static com.mndk.bteterrarenderer.mcconnector.client.mcfx.dropdown.McFXDropdown.*;

@ToString
@RequiredArgsConstructor
class McFXDropdownValueWrapper extends McFXDropdownItem {
    final McFXDropdown parent;
    final String id;

    @Override
    int calculateHeight(@NotNull Stack<String> categoryPath) {
        categoryPath.push(this.id);
        String name = parent.getNameWithRoot(categoryPath);
        int result = parent.getDefaultFont().getWordWrappedHeight(name, parent.getItemInnerWidth()) + ITEM_PADDING_VERTICAL * 2;
        categoryPath.pop();
        return this.height = result;
    }

    @Override
    boolean checkMouseHovered(@Nonnull Stack<String> categoryPath, double mouseX, double mouseY) {
        categoryPath.push(this.id);
        boolean result = this.mouseHovered = parent.mouseInHeight(mouseX, mouseY, this.height);
        categoryPath.pop();
        return result;
    }

    @Override
    void mouseIsNotHovered() {
        this.mouseHovered = false;
    }

    @Override
    void drawItem(
            GuiDrawContextWrapper drawContextWrapper,
            @NotNull Stack<String> categoryPath,
            @Nullable String[] selectedCategoryPath,
            int selectedDepth, boolean isLast
    ) {
        categoryPath.push(this.id);
        String name = parent.getNameWithRoot(categoryPath);
        int color = this.mouseHovered ? HOVERED_COLOR : NORMAL_TEXT_COLOR;
        int textLeft = ITEM_PADDING_HORIZONTAL, limit = parent.getItemInnerWidth();

        if (selectedCategoryPath != null
                && selectedCategoryPath.length == selectedDepth + 1
                && Objects.equals(this.id, selectedCategoryPath[selectedDepth])) {
            drawContextWrapper.fillRect(0, 0, parent.getWidth(), this.height, SELECTED_BACKGROUND_COLOR);
        }

        // Get icon
        NativeTextureWrapper iconTextureObject = parent.getIconTextureObjectWithRoot(categoryPath);
        if (iconTextureObject != null) {
            int textHeight = parent.getDefaultFont().getWordWrappedHeight(name, parent.getItemInnerWidth());
            int y = ITEM_PADDING_VERTICAL + textHeight / 2 - ICON_SIZE / 2;
            drawContextWrapper.drawWholeNativeImage(iconTextureObject,
                    textLeft + ICON_MARGIN_LEFT, y, ICON_SIZE, ICON_SIZE);
            limit -= ICON_SIZE + ICON_MARGIN_LEFT + ICON_MARGIN_RIGHT;
            textLeft += ICON_SIZE + ICON_MARGIN_LEFT + ICON_MARGIN_RIGHT;
        }

        // Item text
        drawContextWrapper.drawWidthSplitText(parent.getDefaultFont(), name, textLeft, ITEM_PADDING_VERTICAL, limit, color);

        // Translate
        drawContextWrapper.translate(0, this.height, 0);
        categoryPath.pop();
    }

    @Override
    void mouseClicked(@NotNull Stack<String> categoryPath) {
        categoryPath.push(this.id);
        if (this.mouseHovered) {
            parent.setSelectedCategoryPath(categoryPath);
        }
        categoryPath.pop();
    }
}