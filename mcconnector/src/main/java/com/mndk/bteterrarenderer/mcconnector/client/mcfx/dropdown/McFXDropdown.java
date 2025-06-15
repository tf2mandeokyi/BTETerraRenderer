package com.mndk.bteterrarenderer.mcconnector.client.mcfx.dropdown;

import com.mndk.bteterrarenderer.mcconnector.client.graphics.NativeTextureWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.shape.GraphicsQuad;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.vertex.PosXY;
import com.mndk.bteterrarenderer.mcconnector.client.gui.GuiDrawContextWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.mcfx.McFXElement;
import com.mndk.bteterrarenderer.util.accessor.PropertyAccessor;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.function.Function;

@RequiredArgsConstructor
public class McFXDropdown extends McFXElement {

    static final int SELECTED_BACKGROUND_COLOR = 0xDFA0AFFF;

    static final int ITEM_PADDING_HORIZONTAL = 12;
    static final int ITEM_PADDING_VERTICAL = 5;
    static final int ITEM_CATEGORY_PADDING_TOP = 2;
    static final int ICON_SIZE = 12;
    static final int ICON_MARGIN_LEFT = -6;
    static final int ICON_MARGIN_RIGHT = 4;

    static final int DROPDOWN_PADDING_TOP = 8;
    static final int DROPDOWN_BACKGROUND_COLOR = 0xE8080808;

    static final int MAINBOX_PADDING_HORIZONTAL = 12;
    static final int MAINBOX_PADDING_VERTICAL = 7;
    static final int MAINBOX_BACKGROUND_COLOR = 0x80000000;
    static final int MAINBOX_BORDER_COLOR = 0xFFFFFFFF;

    static final int ITEMLIST_SEPARATOR_LINE_COLOR = 0xA0FFFFFF;

    static final String ROOT_CATEGORY_NAME = "root";

    private final McFXDropdownItemList dropdownItems = new McFXDropdownItemList(this, ROOT_CATEGORY_NAME, true);
    private final PropertyAccessor<String[]> selectedCategoryPath;
    private final Function<String[], String> nameGetter;
    private final Function<String[], NativeTextureWrapper> iconTextureObjectGetter;

    private boolean mouseOnMainBox = false;
    @Getter(AccessLevel.PACKAGE)
    private int mainBoxHeight, singleLineElementHeight, itemInnerWidth;


    public ItemListUpdater itemListBuilder() {
        return new ItemListUpdater();
    }

    @Override
    protected void init() {
        this.mainBoxHeight = getDefaultFont().getHeight() + MAINBOX_PADDING_VERTICAL * 2;
        this.singleLineElementHeight = getDefaultFont().getHeight() + ITEM_PADDING_VERTICAL * 2;
    }

    @Override
    protected void onWidthChange() {
        this.itemInnerWidth = this.getWidth() - ITEM_PADDING_HORIZONTAL * 2;
    }

    @Override
    public int getPhysicalHeight() {
        return this.mainBoxHeight;
    }

    @Override
    public int getVisualHeight() {
        return this.mainBoxHeight + this.dropdownItems.calculateHeight(new Stack<>());
    }

    @Override
    public boolean mouseHovered(int mouseX, int mouseY, float partialTicks, boolean mouseHidden) {
        this.mouseOnMainBox = !mouseHidden && this.mouseInHeight(mouseX, mouseY, mainBoxHeight);
        if (!this.isOpened()) return this.mouseOnMainBox;

        if (!mouseHidden && this.mouseInHeight(mouseX, mouseY, this.getVisualHeight())) {
            return this.dropdownItems.checkMouseHovered(new Stack<>(), mouseX, mouseY - this.mainBoxHeight) || this.mouseOnMainBox;
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

    boolean mouseInHeight(double mouseX, double mouseY, double height) {
        return mouseX >= 0 && mouseX <= this.getWidth() && mouseY >= 0 && mouseY <= height;
    }

    private String[] getPathWithoutRoot(Stack<String> pathWithRoot) {
        if (pathWithRoot == null || pathWithRoot.isEmpty()) return null;
        String[] pathWithoutRoot = new String[pathWithRoot.size() - 1];
        for (int i = 1; i < pathWithRoot.size(); i++) {
            pathWithoutRoot[i - 1] = pathWithRoot.get(i);
        }
        return pathWithoutRoot;
    }

    void setSelectedCategoryPath(Stack<String> pathWithRoot) {
        String[] pathWithoutRoot = pathWithRoot != null ? this.getPathWithoutRoot(pathWithRoot) : null;
        if (pathWithoutRoot == null) throw new IllegalArgumentException("Path cannot be null or empty");
        this.selectedCategoryPath.set(pathWithoutRoot);
    }

    String getNameWithRoot(Stack<String> pathWithRoot) {
        return nameGetter.apply(this.getPathWithoutRoot(pathWithRoot));
    }

    NativeTextureWrapper getIconTextureObjectWithRoot(Stack<String> pathWithRoot) {
        return iconTextureObjectGetter.apply(this.getPathWithoutRoot(pathWithRoot));
    }

    @Override
    public void drawElement(GuiDrawContextWrapper drawContextWrapper) {
        int mainBoxColor = this.mouseOnMainBox ? HOVERED_COLOR : MAINBOX_BORDER_COLOR;
        boolean opened = this.isOpened();

        // Background
        drawContextWrapper.fillRect(0, 0, this.getWidth(), mainBoxHeight, MAINBOX_BACKGROUND_COLOR);
        if (opened) {
            drawContextWrapper.fillRect(0, mainBoxHeight, this.getWidth(), getVisualHeight(),
                    DROPDOWN_BACKGROUND_COLOR);
        }

        // Dropdown arrow
        this.drawDropdownArrow(drawContextWrapper, MAINBOX_PADDING_VERTICAL, mainBoxColor, opened);

        // Main box Border
        drawContextWrapper.fillRect(-1, -1, 0, mainBoxHeight + 1, mainBoxColor);
        drawContextWrapper.fillRect(0, -1, this.getWidth(), 0, mainBoxColor);
        drawContextWrapper.fillRect(this.getWidth(), -1, this.getWidth() + 1, mainBoxHeight + 1, mainBoxColor);
        drawContextWrapper.fillRect(0, mainBoxHeight, this.getWidth(), mainBoxHeight + 1, mainBoxColor);

        String[] selectedWithoutRoot = selectedCategoryPath.get();
        String currentName = nameGetter.apply(selectedWithoutRoot).replace("\n", " ");
        int fontHeight = getDefaultFont().getHeight();
        int textLeft = MAINBOX_PADDING_HORIZONTAL, limit = itemInnerWidth - fontHeight;

        // Get icon
        NativeTextureWrapper iconTextureObject = this.iconTextureObjectGetter.apply(selectedWithoutRoot);
        if (iconTextureObject != null) {
            int y = MAINBOX_PADDING_VERTICAL + fontHeight / 2 - ICON_SIZE / 2;
            drawContextWrapper.drawWholeNativeImage(iconTextureObject,
                    textLeft + ICON_MARGIN_LEFT, y, ICON_SIZE, ICON_SIZE);
            limit -= ICON_SIZE + ICON_MARGIN_LEFT + ICON_MARGIN_RIGHT;
            textLeft += ICON_SIZE + ICON_MARGIN_LEFT + ICON_MARGIN_RIGHT;
        }

        // Handle overflow
        if (getDefaultFont().getWidth(currentName) > limit) {
            currentName = getDefaultFont().trimToWidth(currentName, limit);
        }
        drawContextWrapper.drawTextWithShadow(getDefaultFont(), currentName, textLeft, MAINBOX_PADDING_VERTICAL, mainBoxColor);

        String[] selected = new String[selectedWithoutRoot.length + 1];
        System.arraycopy(selectedWithoutRoot, 0, selected, 1, selectedWithoutRoot.length);
        selected[0] = ROOT_CATEGORY_NAME;

        this.dropdownItems.calculateHeight(new Stack<>());
        drawContextWrapper.pushMatrix();
        drawContextWrapper.translate(0, mainBoxHeight, 0);
        this.dropdownItems.drawItem(drawContextWrapper, new Stack<>(), selected, 0, true);
        drawContextWrapper.popMatrix();
    }

    void drawDropdownArrow(GuiDrawContextWrapper drawContextWrapper, int top, int colorARGB, boolean flip) {
        int bottom = top + getDefaultFont().getHeight();
        int right = this.getWidth() - MAINBOX_PADDING_HORIZONTAL;
        int left = this.getWidth() - MAINBOX_PADDING_HORIZONTAL - getDefaultFont().getHeight();

        if (flip) {
            int temp = top; top = bottom; bottom = temp;
            temp = right; right = left; left = temp;
        }

        GraphicsQuad<PosXY> quad = new GraphicsQuad<>(
                new PosXY(left, top),
                new PosXY((left + right) / 2f, bottom),
                new PosXY((left + right) / 2f, bottom),
                new PosXY(right, top)
        );
        drawContextWrapper.fillQuad(quad, colorARGB, 0);
    }

    @Override
    public boolean mousePressed(double mouseX, double mouseY, int mouseButton) {
        if (!this.mouseHovered((int) mouseX, (int) mouseY, 0, false)) return false;

        if (this.mouseOnMainBox) {
            this.toggleOpened();
            return true;
        }

        Stack<String> categoryPath = new Stack<>();
        this.dropdownItems.mouseClicked(categoryPath);
        return true;
    }

    public class ItemListUpdater {
        private final List<McFXDropdownItem> list = new ArrayList<>();
        private final Stack<McFXDropdownItemList> stack = new Stack<>();
        private boolean finalized = false;

        public void add(String id) {
            this.validateNonFinalization();
            this.addItem(new McFXDropdownValueWrapper(McFXDropdown.this, id));
        }

        private void addItem(McFXDropdownItem item) {
            this.validateNonFinalization();
            if (stack.isEmpty()) list.add(item);
            else stack.peek().itemList.add(item);
        }

        public void push(String categoryName) {
            this.validateNonFinalization();
            stack.push(new McFXDropdownItemList(McFXDropdown.this, categoryName, false));
        }

        public void pop() {
            this.validateNonFinalization();
            if (stack.isEmpty()) throw new RuntimeException("stack size == 0");

            McFXDropdownItemList list = stack.peek();
            McFXDropdownItemList victim = dropdownItems;
            for (int i = 0; i < stack.size(); i++) {
                if (victim != null) victim = victim.findCategory(stack.get(0).name);
            }
            if (victim != null) list.opened = victim.opened;

            stack.pop();
            this.addItem(list);
        }

        public void update() {
            this.validateNonFinalization();
            if (!stack.isEmpty()) throw new RuntimeException("stack size != 0");

            dropdownItems.itemList.clear();
            dropdownItems.itemList.addAll(list);
            finalized = true;
        }

        public void validateNonFinalization() {
            if (finalized) throw new RuntimeException("updater already finalized");
        }
    }
}
