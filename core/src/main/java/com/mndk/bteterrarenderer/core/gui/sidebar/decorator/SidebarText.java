package com.mndk.bteterrarenderer.core.gui.sidebar.decorator;

import com.mndk.bteterrarenderer.core.gui.FontManager;
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
        this.formattedStringList = FontManager.listFormattedStringToWidth(displayString, parent.elementWidth.get().intValue());
    }

    @Override
    public void onWidthChange(double newWidth) {
        this.formattedStringList = FontManager.listFormattedStringToWidth(displayString, (int) newWidth);
    }

    @Override
    public int getPhysicalHeight() {
        return formattedStringList.size() * FontManager.getFontHeight();
    }

    @Override
    public void drawComponent(Object poseStack) {
        int elementWidth = parent.elementWidth.get().intValue();

        for(int i = 0; i < formattedStringList.size(); ++i) {
            String line = formattedStringList.get(i);
            if(align == TextAlign.LEFT) {
                FontManager.drawStringWithShadow(poseStack,
                        line,
                        0, i * FontManager.getFontHeight(), NORMAL_TEXT_COLOR
                );
            }
            else if(align == TextAlign.RIGHT) {
                FontManager.drawStringWithShadow(poseStack,
                        line,
                        elementWidth - FontManager.getStringWidth(line),
                        i * FontManager.getFontHeight(), NORMAL_TEXT_COLOR
                );
            }
            else if(align == TextAlign.CENTER) {
                FontManager.drawCenteredStringWithShadow(poseStack,
                        line,
                        elementWidth / 2f, i * FontManager.getFontHeight(), NORMAL_TEXT_COLOR
                );
            }
        }
    }

    public enum TextAlign {
        LEFT, CENTER, RIGHT
    }
}
