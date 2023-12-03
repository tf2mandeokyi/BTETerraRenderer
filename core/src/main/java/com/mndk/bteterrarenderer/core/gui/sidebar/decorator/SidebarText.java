package com.mndk.bteterrarenderer.core.gui.sidebar.decorator;

import com.mndk.bteterrarenderer.mcconnector.gui.IFont;
import com.mndk.bteterrarenderer.core.gui.TextAlign;
import com.mndk.bteterrarenderer.core.gui.sidebar.GuiSidebarElement;

import java.util.List;

public class SidebarText extends GuiSidebarElement {

    public final String displayString;
    public final TextAlign align;
    public final int color;
    private List<String> formattedStringList;

    public SidebarText(String displayString, TextAlign align, int color) {
        this.displayString = displayString;
        this.color = color;
        this.align = align;
    }

    public SidebarText(String displayString, TextAlign align) {
        this(displayString, align, NORMAL_TEXT_COLOR);
    }

    @Override
    protected void init() {
        this.formattedStringList = IFont.DEFAULT.splitStringByWidth(displayString, this.getWidth());
    }

    @Override
    public void onWidthChange() {
        this.formattedStringList = IFont.DEFAULT.splitStringByWidth(displayString, this.getWidth());
    }

    @Override
    public int getPhysicalHeight() {
        return formattedStringList.size() * IFont.DEFAULT.getHeight();
    }

    @Override
    public void drawComponent(Object poseStack) {
        for(int i = 0; i < formattedStringList.size(); ++i) {
            String line = formattedStringList.get(i);
            IFont.DEFAULT.drawStringWithShadow(poseStack, line, this.align,
                    0, i * IFont.DEFAULT.getHeight(), this.getWidth(), NORMAL_TEXT_COLOR);
        }
    }
}
