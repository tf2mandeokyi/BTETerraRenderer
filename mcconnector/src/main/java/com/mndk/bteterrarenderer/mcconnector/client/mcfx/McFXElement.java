package com.mndk.bteterrarenderer.mcconnector.client.mcfx;

import com.mndk.bteterrarenderer.mcconnector.McConnector;
import com.mndk.bteterrarenderer.mcconnector.client.gui.GuiDrawContextWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.gui.HorizontalAlign;
import com.mndk.bteterrarenderer.mcconnector.client.gui.component.GuiComponentCopy;
import com.mndk.bteterrarenderer.mcconnector.client.text.StyleWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.text.TextWrapper;
import com.mndk.bteterrarenderer.util.Loggers;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

@Accessors(chain = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class McFXElement implements GuiComponentCopy {

    public boolean hide = false;
    /**
     * Default, non-initialized value is -1
     */
    @Getter(AccessLevel.PROTECTED)
    private int width = -1;

    @Setter private int backgroundColor = 0x00000000;
    @Setter private int color = NORMAL_TEXT_COLOR;
    @Setter private HorizontalAlign align = HorizontalAlign.LEFT;
    private boolean initialized = false;
    private TextWrapper textContent = null;
    private List<? extends TextWrapper> lineComponents;
    private StyleWrapper hoveredStyleComponent;
    private int hoverX, hoverY;

    public final void init(int width) {
        this.width = width;
        if (!this.initialized) this.init();
        if (width > 0) {
            this.onWidthChange();
            this.updateLineSplits();
        }
        this.initialized = true;
    }
    public final void onWidthChange(int width) {
        this.width = width;
        if (width > 0) {
            this.onWidthChange();
            this.updateLineSplits();
        }
    }

    protected abstract void init();

    /** This function is called both in initialization and on width change. */
    public abstract void onWidthChange();

    protected abstract void drawElement(GuiDrawContextWrapper drawContextWrapper);

    public int getPhysicalHeight() {
        return getDefaultFont().getHeight() * this.lineComponents.size();
    }

    /** This shouldn't return something less than {@link McFXElement#getPhysicalHeight()} */
    public int getVisualHeight() {
        return this.getPhysicalHeight();
    }

    @Override
    public boolean mouseHovered(int mouseX, int mouseY, float partialTicks, boolean mouseHidden) {
        if (mouseHidden) {
            this.hoveredStyleComponent = null;
            return false;
        }
        this.hoveredStyleComponent = this.getStyleComponentAt(mouseX, mouseY);
        if (this.hoveredStyleComponent != null) {
            this.hoverX = mouseX;
            this.hoverY = mouseY;
            return true;
        }
        return false;
    }

    @Override
    public boolean mousePressed(double mouseX, double mouseY, int mouseButton) {
        StyleWrapper clickedStyle = this.getStyleComponentAt((int) mouseX, (int) mouseY);
        if (clickedStyle == null) return false;
        return McConnector.client().textManager.handleClick(clickedStyle);
    }

    @Override
    public final void drawComponent(GuiDrawContextWrapper drawContextWrapper) {
        int height = this.getPhysicalHeight();
        drawContextWrapper.fillRect(0, 0, this.getWidth(), height, this.backgroundColor);

        if (this.textContent != null) {
            for (int i = 0; i < lineComponents.size(); ++i) {
                drawContextWrapper.drawTextWithShadow(getDefaultFont(), lineComponents.get(i), this.align,
                        0, i * getDefaultFont().getHeight(), this.getWidth(), this.color);
            }
            if (this.hoveredStyleComponent != null) {
                drawContextWrapper.drawHoverEvent(this.hoveredStyleComponent, hoverX, hoverY);
            }
        }

        this.drawElement(drawContextWrapper);
    }

    public McFXElement setI18nKeyContent(String i18nKey) {
        return this.setStringContent(McConnector.common().i18nManager.format(i18nKey));
    }

    public McFXElement setStringContent(String text) {
        return this.setTextContent(McConnector.client().textManager.fromString(text));
    }

    @SuppressWarnings("UnusedReturnValue")
    public McFXElement setTextJsonContent(@Nullable String json) {
        if (json == null) {
            this.setTextContent(null);
            return this;
        }
        try {
            this.textContent = McConnector.client().textManager.fromJson(json);
            this.updateLineSplits();
        } catch (Exception e) {
            Loggers.get(this).error("Cannot set json text content", e);
            this.setTextContent(null);
        }
        return this;
    }

    public McFXElement setTextContent(TextWrapper text) {
        this.textContent = text;
        this.updateLineSplits();
        return this;
    }

    private void updateLineSplits() {
        if (this.textContent == null || this.getWidth() <= 0) {
            this.lineComponents = Collections.emptyList();
            return;
        }
        this.lineComponents = getDefaultFont().splitByWidth(textContent, this.getWidth());
    }

    @Nullable
    private StyleWrapper getStyleComponentAt(int mouseX, int mouseY) {
        if (mouseX < 0 || this.getWidth() < mouseX) return null;

        int lineIndex = (int) Math.floor(mouseY / (float) getDefaultFont().getHeight());
        if (lineIndex < 0 || this.lineComponents.size() <= lineIndex) return null;
        TextWrapper lineComponent = this.lineComponents.get(lineIndex);

        int xPos = 0;
        int lineWidth = getDefaultFont().getWidth(lineComponent);
        switch (this.align) {
            case LEFT: break;
            case CENTER: xPos = (this.getWidth() - lineWidth) / 2; break;
            case RIGHT: xPos = this.getWidth() - lineWidth; break;
        }
        return getDefaultFont().getStyleComponentFromLine(lineComponent, mouseX - xPos);
    }

    /**
     * Used to calculate Z-axis translation on component drawing step
     **/
    public int getCount() { return 1; }
}

