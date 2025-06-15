package com.mndk.bteterrarenderer.mcconnector.client.mcfx.dropdown;

import com.mndk.bteterrarenderer.mcconnector.client.gui.GuiDrawContextWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Stack;

abstract class McFXDropdownItem {
    protected boolean mouseHovered = false;
    protected int height;

    abstract int calculateHeight(@Nonnull Stack<String> categoryPath);
    /** Pretends itself is at y=0. */
    abstract boolean checkMouseHovered(@Nonnull Stack<String> categoryPath, double mouseX, double mouseY);
    abstract void mouseIsNotHovered();
    /** Translation should be done before this method ends */
    abstract void drawItem(
            GuiDrawContextWrapper drawContextWrapper,
            @Nonnull Stack<String> categoryPath,
            @Nullable String[] selectedCategoryPath,
            int selectedDepth, boolean isLast
    );
    /** This is called after the {@link McFXDropdownItem#checkMouseHovered} call. */
    abstract void mouseClicked(@Nonnull Stack<String> categoryPath);
}