package com.mndk.bteterrarenderer.core.gui.sidebar.dropdown;

import com.mndk.bteterrarenderer.core.graphics.GlGraphicsManager;
import com.mndk.bteterrarenderer.core.graphics.format.PosXY;
import com.mndk.bteterrarenderer.core.graphics.shape.GraphicsQuad;
import com.mndk.bteterrarenderer.core.gui.FontManager;
import com.mndk.bteterrarenderer.core.gui.RawGuiManager;
import com.mndk.bteterrarenderer.core.gui.sidebar.GuiSidebarElement;
import com.mndk.bteterrarenderer.core.util.accessor.PropertyAccessor;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Stack;
import java.util.function.Function;
import java.util.stream.IntStream;

@RequiredArgsConstructor
public class SidebarDropdownSelector<T> extends GuiSidebarElement {

    private static final int SELECTED_BACKGROUND_COLOR = 0xDFA0AFFF;

    private static final int ITEM_PADDING_HORIZONTAL = 12;
    private static final int ITEM_PADDING_VERTICAL = 5;

    private static final int DROPDOWN_PADDING_TOP = 8;
    private static final int DROPDOWN_BACKGROUND_COLOR = 0xE8080808;

    private static final int MAINBOX_PADDING_HORIZONTAL = 12;
    private static final int MAINBOX_PADDING_VERTICAL = 7;
    private static final int MAINBOX_BACKGROUND_COLOR = 0x80000000;
    private static final int MAINBOX_BORDER_COLOR = 0xFFFFFFFF;

    private static final int ITEMLIST_SEPARATOR_LINE_COLOR = 0xA0FFFFFF;


    private final ItemList dropdownItems = new ItemList("main", true);
    private final PropertyAccessor<T> selectedValue;
    private final Function<T, String> nameGetter;

    private boolean mouseOnMainBox = false;
    private int mainBoxHeight, singleLineElementHeight, width, itemInnerWidth;


    public ItemListUpdater itemListBuilder() {
        return new ItemListUpdater();
    }

    @Override
    protected void init() {
        this.mainBoxHeight = FontManager.getFontHeight() + MAINBOX_PADDING_VERTICAL * 2;
        this.singleLineElementHeight = FontManager.getFontHeight() + ITEM_PADDING_VERTICAL * 2;
        this.width = parent.elementWidth.get().intValue();
        this.itemInnerWidth = width - MAINBOX_PADDING_HORIZONTAL * 2;
    }

    @Override
    public int getPhysicalHeight() {
        return this.mainBoxHeight;
    }

    @Override
    public int getVisualHeight() {
        return this.mainBoxHeight + this.dropdownItems.getHeight();
    }

    @Override
    public boolean mouseHovered(double mouseX, double mouseY, float partialTicks, boolean mouseHidden) {
        this.mouseOnMainBox = !mouseHidden && this.mouseInHeight(mouseX, mouseY, mainBoxHeight);
        if(!this.isOpened()) return this.mouseOnMainBox;

        if(!mouseHidden && this.mouseInHeight(mouseX, mouseY, this.getVisualHeight())) {
            return this.dropdownItems.checkMouseHovered(mouseX, mouseY - this.mainBoxHeight) || this.mouseOnMainBox;
        } else {
            this.dropdownItems.mouseIsNotHovered();
            return this.mouseOnMainBox;
        }
    }

    private boolean isOpened() {
        return this.dropdownItems.opened;
    }

    private void toggleOpened() {
        this.dropdownItems.toggleOpened();
    }

    private boolean mouseInHeight(double mouseX, double mouseY, double height) {
        return mouseX >= 0 && mouseX <= width && mouseY >= 0 && mouseY <= height;
    }

    @Override
    public void drawComponent(Object poseStack) {
        int mainBoxColor = this.mouseOnMainBox ? HOVERED_COLOR : MAINBOX_BORDER_COLOR;
        boolean opened = this.isOpened();

        // Background
        RawGuiManager.fillRect(poseStack, 0, 0, width, mainBoxHeight, MAINBOX_BACKGROUND_COLOR);
        if(opened) {
            RawGuiManager.fillRect(poseStack, 0, mainBoxHeight, width, getVisualHeight(),
                    DROPDOWN_BACKGROUND_COLOR);
        }

        // Dropdown arrow
        this.drawDropdownArrow(poseStack, MAINBOX_PADDING_VERTICAL, mainBoxColor, opened);

        // Main box Border
        RawGuiManager.fillRect(poseStack, -1, -1, 0, mainBoxHeight + 1, mainBoxColor);
        RawGuiManager.fillRect(poseStack, 0, -1, width, 0, mainBoxColor);
        RawGuiManager.fillRect(poseStack, width, -1, width + 1, mainBoxHeight + 1, mainBoxColor);
        RawGuiManager.fillRect(poseStack, 0, mainBoxHeight, width, mainBoxHeight + 1, mainBoxColor);

        T selectedValue = this.selectedValue.get();
        if(selectedValue != null) {
            String currentName = nameGetter.apply(selectedValue).replace("\n", " ");
            int limit = itemInnerWidth - FontManager.getFontHeight();
            // Handle overflow
            if(FontManager.getStringWidth(currentName) > limit) {
                currentName = FontManager.trimStringToWidth(currentName, limit);
            }
            FontManager.drawStringWithShadow(poseStack, currentName,
                    MAINBOX_PADDING_HORIZONTAL, MAINBOX_PADDING_VERTICAL, mainBoxColor);
        }

        GlGraphicsManager.glPushMatrix(poseStack);
        GlGraphicsManager.glTranslate(poseStack, 0, mainBoxHeight, 0);
        this.dropdownItems.drawItem(poseStack, selectedValue, true);
        GlGraphicsManager.glPopMatrix(poseStack);
    }

    private void drawDropdownArrow(Object poseStack, int top, int colorARGB, boolean flip) {
        int bottom = top + FontManager.getFontHeight();
        int right = width - MAINBOX_PADDING_HORIZONTAL;
        int left = width - MAINBOX_PADDING_HORIZONTAL - FontManager.getFontHeight();

        if (flip) {
            int temp = top; top = bottom; bottom = temp;
            temp = right; right = left; left = temp;
        }

        GraphicsQuad<PosXY> quad = GraphicsQuad.newPosXY(
                new PosXY(left, top),
                new PosXY((left + right) / 2f, bottom),
                new PosXY((left + right) / 2f, bottom),
                new PosXY(right, top)
        );
        RawGuiManager.fillQuad(poseStack, quad, colorARGB, 0);
    }

    @Override
    public boolean mousePressed(double mouseX, double mouseY, int mouseButton) {
        if(!this.mouseHovered(mouseX, mouseY, 0, false)) return false;

        if(this.mouseOnMainBox) {
            this.toggleOpened();
            return true;
        }

        this.dropdownItems.mouseClicked();
        return true;
    }

    @Override
    public void onWidthChange(double newWidth) {
        this.width = (int) newWidth;
        this.itemInnerWidth = (int) (newWidth - ITEM_PADDING_HORIZONTAL * 2);
    }


    private abstract class DropdownItem {
        boolean mouseHovered = false;
        abstract int getHeight();
        /** Pretends itself is at y=0. */
        abstract boolean checkMouseHovered(double mouseX, double mouseY);
        abstract void mouseIsNotHovered();
        /** Translation should be done before this method ends */
        abstract void drawItem(Object poseStack, T selectedValue, boolean isLast);
        /** This is called after the {@link DropdownItem#checkMouseHovered} call. */
        abstract void mouseClicked();
    }

    @ToString
    @RequiredArgsConstructor
    private class ValueWrapper extends DropdownItem {
        final T value;

        @Override
        int getHeight() {
            return FontManager.getWordWrappedHeight(nameGetter.apply(this.value), itemInnerWidth)
                    + ITEM_PADDING_VERTICAL * 2;
        }

        @Override
        boolean checkMouseHovered(double mouseX, double mouseY) {
            return this.mouseHovered = mouseInHeight(mouseX, mouseY, this.getHeight());
        }

        @Override
        void mouseIsNotHovered() {
            this.mouseHovered = false;
        }

        @Override
        void drawItem(Object poseStack, T selectedValue, boolean isLast) {
            String name = nameGetter.apply(this.value);
            int color = this.mouseHovered ? HOVERED_COLOR : NORMAL_TEXT_COLOR;
            int height = this.getHeight();

            if(Objects.equals(this.value, selectedValue)) {
                RawGuiManager.fillRect(poseStack,
                        0, 0, width, height, SELECTED_BACKGROUND_COLOR);
            }

            // Item text
            FontManager.drawSplitString(poseStack, name,
                    ITEM_PADDING_HORIZONTAL, ITEM_PADDING_VERTICAL, itemInnerWidth, color);

            // Translate
            GlGraphicsManager.glTranslate(poseStack, 0, height, 0);
        }

        @Override
        void mouseClicked() {
            if(this.mouseHovered) selectedValue.set(this.value);
        }
    }

    @RequiredArgsConstructor
    private class ItemList extends DropdownItem {
        boolean opened = false;
        final String name;
        final boolean main;
        List<DropdownItem> itemList = new ArrayList<>();

        ItemList findCategory(String categoryName) {
            for(DropdownItem item : this.itemList) {
                if(item == null) continue;
                if(!(item instanceof SidebarDropdownSelector<?>.ItemList)) continue;
                ItemList category = (ItemList) item;
                if(category.name.equals(categoryName)) return category;
            }
            return null;
        }

        void toggleOpened() {
            this.opened = !this.opened;
        }

        int getCategoryHeight() {
            return this.main ? DROPDOWN_PADDING_TOP : singleLineElementHeight;
        }

        @Override
        int getHeight() {
            return this.getCategoryHeight() + (this.opened ?
                    this.itemList.stream().mapToInt(DropdownItem::getHeight).sum() : 0);
        }

        @Override
        boolean checkMouseHovered(double mouseX, double mouseY) {
            int yOffset = this.getCategoryHeight();
            boolean result = this.mouseHovered = (!this.main && mouseInHeight(mouseX, mouseY, yOffset));

            if(this.opened) for(DropdownItem item : this.itemList) {
                if(result) { item.mouseIsNotHovered(); continue; }
                if(item.checkMouseHovered(mouseX, mouseY - yOffset)) result = true;
                yOffset += item.getHeight();
            }
            return result;
        }

        @Override
        void mouseIsNotHovered() {
            this.mouseHovered = false;
            if(this.opened) this.itemList.forEach(DropdownItem::mouseIsNotHovered);
        }

        @Override
        void drawItem(Object poseStack, T selectedValue, boolean isLast) {
            int categoryColor = this.mouseHovered ? HOVERED_COLOR : NORMAL_TEXT_COLOR;

            if(!this.main) {
                // Category name
                FontManager.drawCenteredStringWithShadow(poseStack,
                        this.name, width / 2.0f, ITEM_PADDING_VERTICAL, categoryColor);
                // Dropdown arrow
                drawDropdownArrow(poseStack, ITEM_PADDING_VERTICAL, categoryColor, this.opened);
            }
            GlGraphicsManager.glTranslate(poseStack, 0, this.getCategoryHeight(), 0);

            if(this.opened) IntStream.range(0, itemList.size()).forEachOrdered(i ->
                    itemList.get(i).drawItem(poseStack, selectedValue, i == itemList.size() - 1));

            if(!isLast) {
                // Category separator line
                RawGuiManager.fillRect(poseStack, 0, 0, width, 1,
                        ITEMLIST_SEPARATOR_LINE_COLOR);
            }
        }

        @Override
        void mouseClicked() {
            if(this.mouseHovered) { this.toggleOpened(); return; }
            this.itemList.forEach(DropdownItem::mouseClicked);
        }
    }


    public class ItemListUpdater {
        private final List<DropdownItem> list = new ArrayList<>();
        private final Stack<ItemList> stack = new Stack<>();
        private boolean finalized = false;

        public void add(T item) {
            this.validateNonFinalization();
            if(item == null) return;
            this.addItem(new ValueWrapper(item));
        }

        private void addItem(DropdownItem item) {
            this.validateNonFinalization();
            if (stack.isEmpty()) list.add(item);
            else stack.peek().itemList.add(item);
        }

        public void push(String categoryName) {
            this.validateNonFinalization();
            stack.push(new ItemList(categoryName, false));
        }

        public void pop() {
            this.validateNonFinalization();
            if(stack.isEmpty()) throw new RuntimeException("stack size == 0");

            ItemList list = stack.peek();
            ItemList victim = dropdownItems;
            for(int i = 0; i < stack.size(); i++) {
                if(victim != null) victim = victim.findCategory(stack.get(0).name);
            }
            if(victim != null) list.opened = victim.opened;

            stack.pop();
            this.addItem(list);
        }

        public void update() {
            this.validateNonFinalization();
            if(!stack.isEmpty()) throw new RuntimeException("stack size != 0");

            dropdownItems.itemList.clear();
            dropdownItems.itemList.addAll(list);
            finalized = true;
        }

        public void validateNonFinalization() {
            if(finalized) throw new RuntimeException("updater already finalized");
        }
    }
}
