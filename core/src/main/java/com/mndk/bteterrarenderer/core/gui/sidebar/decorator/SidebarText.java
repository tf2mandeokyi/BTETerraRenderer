package com.mndk.bteterrarenderer.core.gui.sidebar.decorator;

import com.mndk.bteterrarenderer.core.gui.sidebar.GuiSidebarElement;
import com.mndk.bteterrarenderer.mcconnector.gui.FontRenderer;
import com.mndk.bteterrarenderer.mcconnector.gui.HorizontalAlign;
import com.mndk.bteterrarenderer.mcconnector.wrapper.DrawContextWrapper;

import java.util.List;

public class SidebarText extends GuiSidebarElement {

    public final String displayString;
    public final HorizontalAlign align;
    public final int color;
    private List<String> formattedStringList;

    public SidebarText(String displayString, HorizontalAlign align, int color) {
        this.displayString = displayString;
        this.color = color;
        this.align = align;
    }

    public SidebarText(String displayString, HorizontalAlign align) {
        this(displayString, align, NORMAL_TEXT_COLOR);
    }

    @Override
    protected void init() {
        this.formattedStringList = FontRenderer.DEFAULT.splitStringByWidth(displayString, this.getWidth());
    }

    @Override
    public void onWidthChange() {
        this.formattedStringList = FontRenderer.DEFAULT.splitStringByWidth(displayString, this.getWidth());
    }

    @Override
    public int getPhysicalHeight() {
        return formattedStringList.size() * FontRenderer.DEFAULT.getHeight();
    }

    @Override
    public void drawComponent(DrawContextWrapper drawContextWrapper) {
        for(int i = 0; i < formattedStringList.size(); ++i) {
            String line = formattedStringList.get(i);
            FontRenderer.DEFAULT.drawStringWithShadow(drawContextWrapper, line, this.align,
                    0, i * FontRenderer.DEFAULT.getHeight(), this.getWidth(), this.color);
        }
    }
}
