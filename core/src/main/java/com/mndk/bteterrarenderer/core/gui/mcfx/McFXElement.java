package com.mndk.bteterrarenderer.core.gui.mcfx;

import com.mndk.bteterrarenderer.core.util.Loggers;
import com.mndk.bteterrarenderer.mcconnector.gui.HorizontalAlign;
import com.mndk.bteterrarenderer.mcconnector.gui.component.GuiComponentCopy;
import com.mndk.bteterrarenderer.mcconnector.gui.text.TextManager;
import com.mndk.bteterrarenderer.mcconnector.i18n.I18nManager;
import com.mndk.bteterrarenderer.mcconnector.wrapper.DrawContextWrapper;
import com.mndk.bteterrarenderer.mcconnector.wrapper.FontWrapper;
import com.mndk.bteterrarenderer.mcconnector.wrapper.StyleWrapper;
import com.mndk.bteterrarenderer.mcconnector.wrapper.TextWrapper;
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
public abstract class McFXElement extends GuiComponentCopy {

    public boolean hide = false;
    /**
     * Default, non-initialized value is -1
     */
    @Getter(AccessLevel.PROTECTED)
    private int width = -1;

    @Setter private int backgroundColor = 0x00000000;
    @Setter private int color = NORMAL_TEXT_COLOR;
    @Setter private HorizontalAlign align;
    private TextWrapper textContent = null;
    private List<TextWrapper> lineComponents;
    private StyleWrapper hoveredStyleComponent;
    private int hoverX, hoverY;

    public final void init(int width) {
        this.width = width;
        this.init();
        this.updateLineSplits();
        if(width > 0) this.onWidthChange();
    }
    public final void onWidthChange(int width) {
        this.width = width;
        this.updateLineSplits();
        if(width > 0) this.onWidthChange();
    }

    protected abstract void init();

    /** This function is called both in initialization and on width change. */
    public abstract void onWidthChange();

    protected abstract void drawElement(DrawContextWrapper<?> drawContextWrapper);

    public int getPhysicalHeight() {
        return FontWrapper.DEFAULT.getHeight() * this.lineComponents.size();
    }

    /** This shouldn't return something less than {@link McFXElement#getPhysicalHeight()} */
    public int getVisualHeight() {
        return this.getPhysicalHeight();
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
    public boolean mousePressed(double mouseX, double mouseY, int mouseButton) {
        StyleWrapper clickedStyle = this.getStyleComponentAt((int) mouseX, (int) mouseY);
        if(clickedStyle == null) return false;
        return TextManager.INSTANCE.handleClick(clickedStyle);
    }

    @Override
    public final void drawComponent(DrawContextWrapper<?> drawContextWrapper) {
        int height = this.getPhysicalHeight();
        drawContextWrapper.fillRect(0, 0, this.getWidth(), height, this.backgroundColor);

        if(this.textContent != null) {
            for(int i = 0; i < lineComponents.size(); ++i) {
                drawContextWrapper.drawTextWithShadow(FontWrapper.DEFAULT, lineComponents.get(i), this.align,
                        0, i * FontWrapper.DEFAULT.getHeight(), this.getWidth(), this.color);
            }
            if(this.hoveredStyleComponent != null) {
                drawContextWrapper.drawHoverEvent(this.hoveredStyleComponent, hoverX, hoverY);
            }
        }

        this.drawElement(drawContextWrapper);
    }

    public McFXElement setI18nKeyContent(String i18nKey) {
        return this.setStringContent(I18nManager.format(i18nKey));
    }

    public McFXElement setStringContent(String text) {
        return this.setTextContent(TextManager.INSTANCE.fromString(text));
    }

    @SuppressWarnings("UnusedReturnValue")
    public McFXElement setTextJsonContent(String json) {
        try {
            this.textContent = TextManager.INSTANCE.fromJson(json);
            this.updateLineSplits();
        } catch(Exception e) {
            Loggers.get(this).error(e);
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
        if(this.textContent == null || this.getWidth() == -1) {
            this.lineComponents = Collections.emptyList();
            return;
        }
        this.lineComponents = FontWrapper.DEFAULT.splitByWidth(textContent, this.getWidth());
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

    /**
     * Used to calculate Z-axis translation on component drawing step
     **/
    public int getCount() { return 1; }
}

