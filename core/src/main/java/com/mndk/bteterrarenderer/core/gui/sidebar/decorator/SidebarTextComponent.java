package com.mndk.bteterrarenderer.core.gui.sidebar.decorator;

import com.mndk.bteterrarenderer.core.gui.sidebar.GuiSidebarElement;
import com.mndk.bteterrarenderer.core.util.Loggers;
import com.mndk.bteterrarenderer.mcconnector.wrapper.FontWrapper;
import com.mndk.bteterrarenderer.mcconnector.gui.HorizontalAlign;
import com.mndk.bteterrarenderer.mcconnector.gui.text.TextManager;
import com.mndk.bteterrarenderer.mcconnector.wrapper.DrawContextWrapper;
import com.mndk.bteterrarenderer.mcconnector.wrapper.StyleWrapper;
import com.mndk.bteterrarenderer.mcconnector.wrapper.TextWrapper;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class SidebarTextComponent extends GuiSidebarElement {

    private String textComponentJson;
    private List<TextWrapper> lineComponents;
    private StyleWrapper hoveredStyleComponent;
    private final HorizontalAlign align;
    private int hoverX, hoverY;

    public SidebarTextComponent(HorizontalAlign align) {
        this("[\"\"]", align);
    }

    public SidebarTextComponent(String textComponentJson, HorizontalAlign align) {
        this.textComponentJson = textComponentJson;
        this.align = align;
        this.updateLineSplits();
    }

    @Override
    protected void init() {}

    @Override
    public void onWidthChange() {
        this.updateLineSplits();
    }

    @Override
    public boolean mouseHovered(double mouseX, double mouseY, float partialTicks, boolean mouseHidden) {
        if(mouseHidden) {
            this.hoveredStyleComponent = null;
            return false;
        }
        this.hoveredStyleComponent = this.getStyleComponentAt((int) mouseX, (int) mouseY);
        if(this.hoveredStyleComponent != null) {
            this.hoverX = (int) mouseX;
            this.hoverY = (int) mouseY;
            return true;
        }
        return false;
    }

    @Override
    public void drawComponent(DrawContextWrapper<?> drawContextWrapper) {
        for(int i = 0; i < lineComponents.size(); ++i) {
            drawContextWrapper.drawTextWithShadow(FontWrapper.DEFAULT, lineComponents.get(i), this.align,
                    0, i * FontWrapper.DEFAULT.getHeight(), this.getWidth(), NORMAL_TEXT_COLOR);
        }
        if(this.hoveredStyleComponent != null) {
            drawContextWrapper.drawHoverEvent(this.hoveredStyleComponent, hoverX, hoverY);
        }
    }

    @Override
    public int getPhysicalHeight() {
        return FontWrapper.DEFAULT.getHeight() * lineComponents.size();
    }

    @Override
    public boolean mousePressed(double mouseX, double mouseY, int mouseButton) {
        StyleWrapper clickedStyle = this.getStyleComponentAt((int) mouseX, (int) mouseY);
        if(clickedStyle == null) return false;
        return TextManager.INSTANCE.handleClick(clickedStyle);
    }

    public void setTextComponentJson(String newJson) {
        this.textComponentJson = newJson;
        this.updateLineSplits();
    }

    private void updateLineSplits() {
        this.lineComponents = this.getLineSplits();
    }

    private List<TextWrapper> getLineSplits() {
        if(this.textComponentJson == null || this.getWidth() == -1) return Collections.emptyList();

        try {
            TextWrapper textComponent = TextManager.INSTANCE.fromJson(this.textComponentJson);
            if (textComponent == null) return Collections.emptyList();

            return FontWrapper.DEFAULT.splitByWidth(textComponent, this.getWidth());
        } catch(Exception e) {
            Loggers.get(this).error(e);
            return Collections.emptyList();
        }
    }

    @Nullable
    private StyleWrapper getStyleComponentAt(int mouseX, int mouseY) {
        if(mouseX < 0 || this.getWidth() < mouseX) return null;

        int lineIndex = (int) Math.floor(mouseY / (float) FontWrapper.DEFAULT.getHeight());
        if(lineIndex < 0 || this.lineComponents.size() <= lineIndex) return null;
        TextWrapper lineComponent = this.lineComponents.get(lineIndex);

        int xPos = 0;
        int lineWidth = FontWrapper.DEFAULT.getWidth(lineComponent);
        switch(this.align) {
            case LEFT: break;
            case CENTER: xPos = (this.getWidth() - lineWidth) / 2; break;
            case RIGHT: xPos = this.getWidth() - lineWidth; break;
        }
        return FontWrapper.DEFAULT.getStyleComponentFromLine(lineComponent, mouseX - xPos);
    }
}
