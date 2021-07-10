package com.mndk.bteterrarenderer.gui.sidebar.elem;

import com.mndk.bteterrarenderer.gui.sidebar.GuiSidebarElement;
import com.mndk.bteterrarenderer.util.GetterSetter;
import net.minecraft.client.gui.Gui;

import java.io.IOException;
import java.util.function.Function;

public class SidebarDropdownSelector<T> extends GuiSidebarElement {



    private static final int HORIZONTAL_PADDING = 12;
    private static final int VERTICAL_PADDING = 7;

    private static final int ELEMENT_VERTICAL_MARGIN = 5;
    private static final int DROPDOWN_VERTICAL_PADDING = 8;



    private final GetterSetter<T> value;
    private final Function<T, String> nameGetter;
    private final Object[] values;
    private boolean opened = false;

    private int height, elementHeight;



    public SidebarDropdownSelector(GetterSetter<T> value, Function<T, String> nameGetter, Object[] values) {
        this.value = value;
        this.nameGetter = nameGetter;
        this.values = values;
    }



    @Override
    protected void init() {
        this.height = fontRenderer.FONT_HEIGHT + VERTICAL_PADDING * 2;
        this.elementHeight = fontRenderer.FONT_HEIGHT + ELEMENT_VERTICAL_MARGIN * 2;
    }



    @Override
    public int getHeight() {
        return opened ?
                this.height + (values.length * this.elementHeight) + DROPDOWN_VERTICAL_PADDING :
                this.height;
    }



    @Override
    @SuppressWarnings( "unchecked")
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {

        this.drawBackground();

        int width = parent.elementWidth.get();
        int rectColor = mouseInBox(mouseX, mouseY) ? 0xFFFFFFA0 : 0xFFFFFFFF;

        Gui.drawRect(-1, -1, 0, height + 1, rectColor);
        Gui.drawRect(-1, -1, width, 0, rectColor);
        Gui.drawRect(width, -1, width + 1, height + 1, rectColor);
        Gui.drawRect(-1, height, width + 1, height + 1, rectColor);

        this.drawString(fontRenderer, nameGetter.apply(value.get()), HORIZONTAL_PADDING, VERTICAL_PADDING, rectColor);

        if(opened) {
            int i = 0;
            int yStart = height + DROPDOWN_VERTICAL_PADDING;

            for(Object object : values) {
                if(object instanceof String) {
                    String categoryName = (String) object;
                    this.drawCenteredString(this.fontRenderer, categoryName,
                            parent.elementWidth.get() / 2,
                            yStart + elementHeight * i + ELEMENT_VERTICAL_MARGIN,
                            0xFFFFFF
                    );
                }
                else if(object != null) {
                    T element;
                    try { element = (T) object; } catch (ClassCastException ignored) { continue; }
                    String name = nameGetter.apply(element);
                    int color = isMouseOnIndex(mouseX, mouseY, i) ? 0xFFFFA0 : 0xFFFFFF;

                    if(value.get().equals(object)) {
                        Gui.drawRect(
                                0, yStart + elementHeight * i,
                                parent.elementWidth.get(), yStart + elementHeight * (i + 1),
                                0xDFA0AFFF
                        );
                    }

                    this.drawString(
                            this.fontRenderer, name,
                            HORIZONTAL_PADDING, yStart + elementHeight * i + ELEMENT_VERTICAL_MARGIN,
                            color
                    );
                }
                ++i;
            }
        }
    }



    private void drawBackground() {
        Gui.drawRect(0, 0, parent.elementWidth.get(), height, 0x50000000);
        if(opened) {
            Gui.drawRect(0, height, parent.elementWidth.get(), getHeight(), 0x80000000);
        }
    }



    @Override
    @SuppressWarnings("unchecked")
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if(mouseInBox(mouseX, mouseY)) {
            opened = !opened;
            return true;
        }
        else {
            int i = 0;
            for(Object o : values) {
                if(!(o instanceof String)) {
                    if(isMouseOnIndex(mouseX, mouseY, i)) {
                        value.set((T) o);
                        return true;
                    }
                }
                ++i;
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
