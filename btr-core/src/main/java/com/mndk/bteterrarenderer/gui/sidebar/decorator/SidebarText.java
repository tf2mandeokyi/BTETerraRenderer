package com.mndk.bteterrarenderer.gui.sidebar.decorator;

import com.mndk.bteterrarenderer.connector.gui.FontConnector;
import com.mndk.bteterrarenderer.gui.sidebar.GuiSidebarElement;

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
        this(displayString, align, 0xFFFFFF);
    }

    @Override
    protected void init() {
        this.formattedStringList = FontConnector.INSTANCE.listFormattedStringToWidth(displayString, parent.elementWidth.get().intValue());
    }

    @Override
    public void onWidthChange(double newWidth) {
        this.formattedStringList = FontConnector.INSTANCE.listFormattedStringToWidth(displayString, (int) newWidth);
    }

    @Override
    public int getHeight() {
        return formattedStringList.size() * FontConnector.INSTANCE.getFontHeight();
    }

    @Override
    public void drawComponent(Object poseStack, double mouseX, double mouseY, float partialTicks) {
        int elementWidth = parent.elementWidth.get().intValue();

        for(int i = 0; i < formattedStringList.size(); ++i) {
            String line = formattedStringList.get(i);
            if(align == TextAlign.LEFT) {
                FontConnector.INSTANCE.drawStringWithShadow(poseStack,
                        line,
                        0, i * FontConnector.INSTANCE.getFontHeight(), 0xFFFFFF
                );
            }
            else if(align == TextAlign.RIGHT) {
                FontConnector.INSTANCE.drawStringWithShadow(poseStack,
                        line,
                        elementWidth - FontConnector.INSTANCE.getStringWidth(line),
                        i * FontConnector.INSTANCE.getFontHeight(), 0xFFFFFF
                );
            }
            else if(align == TextAlign.CENTER){
                FontConnector.INSTANCE.drawCenteredStringWithShadow(poseStack,
                        line,
                        elementWidth / 2f, i * FontConnector.INSTANCE.getFontHeight(), 0xFFFFFF
                );
            }
        }
    }

    public enum TextAlign {
        LEFT, CENTER, RIGHT
    }
}
