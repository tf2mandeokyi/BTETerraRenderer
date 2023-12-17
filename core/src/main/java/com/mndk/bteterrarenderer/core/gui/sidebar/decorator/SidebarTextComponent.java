package com.mndk.bteterrarenderer.core.gui.sidebar.decorator;

import com.mndk.bteterrarenderer.mcconnector.gui.FontRenderer;
import com.mndk.bteterrarenderer.mcconnector.gui.HorizontalAlign;
import com.mndk.bteterrarenderer.mcconnector.gui.TextComponentManager;
import com.mndk.bteterrarenderer.core.gui.sidebar.GuiSidebarElement;
import com.mndk.bteterrarenderer.core.util.Loggers;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class SidebarTextComponent extends GuiSidebarElement {

    private String textComponentJson;
    private List<?> lineComponents;
    private Object hoveredStyleComponent;
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
    protected void init() {
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
    public void drawComponent(Object poseStack) {
        for(int i = 0; i < lineComponents.size(); ++i) {
            FontRenderer.DEFAULT.drawComponentWithShadow(poseStack, lineComponents.get(i), this.align,
                    0, i * FontRenderer.DEFAULT.getHeight(), this.getWidth(), NORMAL_TEXT_COLOR);
        }
        if(this.hoveredStyleComponent != null) {
            TextComponentManager.handleStyleComponentHover(poseStack, this.hoveredStyleComponent, hoverX, hoverY);
        }
    }

    @Override
    public int getPhysicalHeight() {
        return FontRenderer.DEFAULT.getHeight() * lineComponents.size();
    }

    @Override
    public void onWidthChange() {
        this.updateLineSplits();
    }

    @Override
    public boolean mousePressed(double mouseX, double mouseY, int mouseButton) {
        Object clickedStyle = this.getStyleComponentAt((int) mouseX, (int) mouseY);
        if(clickedStyle == null) return false;
        return TextComponentManager.handleClick(clickedStyle);
    }

    public void setTextComponentJson(String newJson) {
        this.textComponentJson = newJson;
        this.updateLineSplits();
    }

    private void updateLineSplits() {
        this.lineComponents = this.getLineSplits();
    }

    private List<?> getLineSplits() {
        if(this.textComponentJson == null || this.getWidth() == -1) return Collections.emptyList();

        try {
            Object textComponent = TextComponentManager.fromJson(this.textComponentJson);
            if (textComponent == null) return Collections.emptyList();

            return FontRenderer.DEFAULT.splitComponentByWidth(textComponent, this.getWidth());
        } catch(Exception e) {
            Loggers.get(this).error(e);
            return Collections.emptyList();
        }
    }

    @Nullable
    private Object getStyleComponentAt(int mouseX, int mouseY) {
        if(mouseX < 0 || this.getWidth() < mouseX) return null;

        int lineIndex = (int) Math.floor(mouseY / (float) FontRenderer.DEFAULT.getHeight());
        if(lineIndex < 0 || this.lineComponents.size() <= lineIndex) return null;
        Object lineComponent = this.lineComponents.get(lineIndex);

        int xPos = 0;
        int lineWidth = FontRenderer.DEFAULT.getComponentWidth(lineComponent);
        switch(this.align) {
            case LEFT: break;
            case CENTER: xPos = (this.getWidth() - lineWidth) / 2; break;
            case RIGHT: xPos = this.getWidth() - lineWidth; break;
        }
        return FontRenderer.DEFAULT.getStyleComponentFromLine(lineComponent, mouseX - xPos);
    }
}
