package com.mndk.bteterrarenderer.mcconnector.client.gui.widget;

import com.mndk.bteterrarenderer.mcconnector.McConnector;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.DrawContextWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.input.GameInputManager;
import com.mndk.bteterrarenderer.mcconnector.client.input.InputKey;
import com.mndk.bteterrarenderer.mcconnector.client.text.TextWrapper;
import com.mndk.bteterrarenderer.mcconnector.util.MinecraftStringUtil;
import com.mndk.bteterrarenderer.util.BTRUtil;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Copied from both 1.12.2's <code>net.minecraft.client.gui.GuiTextField</code>
 * and 1.18.2's <code>net.minecraft.client.gui.components.EditBox</code>
 */
public class TextFieldWidgetCopy extends AbstractWidgetCopy {

    private static final int BACKGROUND_COLOR = 0xFF000000;

    @Setter
    private Integer textColor;
    private int maxStringLength = 32;
    private int frame;
    @Getter @Setter
    private boolean drawsBackground = true;
    private boolean shiftPressed;
    @Getter @Setter
    private int displayPos, cursorPos, highlightPos;
    @Nullable
    @Setter private String suggestion;

    @Nullable
    @Setter private Consumer<String> changedListener;
    @Setter private Predicate<String> validator = s -> true;
    @Setter private BiFunction<String, Integer, TextWrapper> renderTextProvider =
            (string, firstCharacterIndex) -> McConnector.client().textManager.fromString(string);

    public TextFieldWidgetCopy(int x, int y, int width, int height) {
        super(x, y, width, height, "");
    }

    public void tick() {
        this.frame++;
    }

    public void setText(String text) {
        if (!this.validator.test(text)) return;

        this.text = text.length() > maxStringLength ? text.substring(0, maxStringLength) : text;
        this.moveCursorToEnd();
        this.setHighlightPos(this.cursorPos);
        this.onChanged(text);
    }

    public String getHighlighted() {
        int start = Math.min(this.cursorPos, this.highlightPos);
        int end = Math.max(this.cursorPos, this.highlightPos);
        return this.text.substring(start, end);
    }

    public void insertText(String text) {
        int start = Math.min(this.cursorPos, this.highlightPos);
        int end = Math.max(this.cursorPos, this.highlightPos);
        int max = this.maxStringLength - this.text.length() - (start - end);
        String filtered = MinecraftStringUtil.filterMinecraftAllowedCharacters(text);
        int length = filtered.length();
        if (max < length) {
            filtered = filtered.substring(0, max);
            length = max;
        }

        String result = (new StringBuilder(this.text)).replace(start, end, filtered).toString();
        if (this.validator.test(result)) {
            this.text = result;
            this.setCursorPosition(start + length);
            this.setHighlightPos(this.cursorPos);
            this.onChanged(this.text);
        }
    }

    private void onChanged(String newText) {
        if (this.changedListener == null) return;
        this.changedListener.accept(newText);
    }

    private void deleteText(int delta) {
        if (McConnector.client().inputManager.isControlKeyDown()) {
            this.deleteWords(delta);
        } else {
            this.deleteChars(delta);
        }
    }

    public void deleteWords(int delta) {
        if (this.text.isEmpty()) return;

        if (this.highlightPos != this.cursorPos) {
            this.insertText("");
        } else {
            this.deleteChars(this.getWordPosition(delta) - this.cursorPos);
        }
    }

    public void deleteChars(int delta) {
        if (this.text.isEmpty()) return;

        if (this.highlightPos != this.cursorPos) {
            this.insertText("");
            return;
        }

        int i = this.getCursorPos(delta);
        int j = Math.min(i, this.cursorPos);
        int k = Math.max(i, this.cursorPos);
        if (j == k) return;

        String s = (new StringBuilder(this.text)).delete(j, k).toString();
        if (!this.validator.test(s)) return;
        this.text = s;
        this.moveCursorTo(j);
    }

    public int getWordPosition(int delta) {
        return this.getWordPosition(delta, this.getCursorPos());
    }

    private int getWordPosition(int delta, int cursorPos) {
        return this.getWordPosition(delta, cursorPos, true);
    }

    @SuppressWarnings("SameParameterValue")
    private int getWordPosition(int delta, int cursorPos, boolean p_94143_) {
        int i = cursorPos;
        boolean flag = delta < 0;
        int j = Math.abs(delta);

        for (int k = 0; k < j; ++k) {
            if (!flag) {
                int l = this.text.length();
                i = this.text.indexOf(32, i);
                if (i == -1) {
                    i = l;
                } else {
                    while (p_94143_ && i < l && this.text.charAt(i) == ' ') {
                        ++i;
                    }
                }
            } else {
                while (p_94143_ && i > 0 && this.text.charAt(i - 1) == ' ') {
                    --i;
                }

                while (i > 0 && this.text.charAt(i - 1) != ' ') {
                    --i;
                }
            }
        }

        return i;
    }

    public void moveCursor(int delta) {
        this.moveCursorTo(this.getCursorPos(delta));
    }

    private int getCursorPos(int delta) {
        return MinecraftStringUtil.offsetByCodepoints(this.text, this.cursorPos, delta);
    }

    public void moveCursorTo(int index) {
        this.setCursorPosition(index);
        if (!this.shiftPressed) {
            this.setHighlightPos(this.cursorPos);
        }
        this.onChanged(this.text);
    }

    public void setCursorPosition(int index) {
        this.cursorPos = BTRUtil.clamp(index, 0, this.text.length());
    }

    public void moveCursorToStart() {
        this.moveCursorTo(0);
    }

    public void moveCursorToEnd() {
        this.moveCursorTo(this.text.length());
    }

    public boolean keyPressed(InputKey key, int scanCode, int modifiers) {
        if (!this.canConsumeInput()) return false;

        GameInputManager inputManager = McConnector.client().inputManager;
        this.shiftPressed = inputManager.isShiftKeyDown();
        if (inputManager.isKeySelectAll(key)) {
            this.moveCursorToEnd();
            this.setHighlightPos(0);
            return true;
        } else if (inputManager.isKeyCopy(key)) {
            inputManager.setClipboardContent(this.getHighlighted());
            return true;
        } else if (inputManager.isKeyPaste(key)) {
            if (this.enabled) {
                this.insertText(inputManager.getClipboardContent());
            }
            return true;
        } else if (inputManager.isKeyCut(key)) {
            inputManager.setClipboardContent(this.getHighlighted());
            if (this.enabled) {
                this.insertText("");
            }
            return true;
        } else switch (key) {
            case KEY_BACKSPACE:
                if (this.enabled) {
                    this.shiftPressed = false;
                    this.deleteText(-1);
                    this.shiftPressed = inputManager.isShiftKeyDown();
                }
                return true;
            case KEY_INSERT:
            case KEY_DOWN:
            case KEY_UP:
            case KEY_PAGE_UP:
            case KEY_PAGE_DOWN:
            default:
                return false;
            case KEY_DELETE:
                if (this.enabled) {
                    this.shiftPressed = false;
                    this.deleteText(1);
                    this.shiftPressed = inputManager.isShiftKeyDown();
                }
                return true;
            case KEY_RIGHT:
                if (inputManager.isControlKeyDown()) {
                    this.moveCursorTo(this.getWordPosition(1));
                } else {
                    this.moveCursor(1);
                }
                return true;
            case KEY_LEFT:
                if (inputManager.isControlKeyDown()) {
                    this.moveCursorTo(this.getWordPosition(-1));
                } else {
                    this.moveCursor(-1);
                }
                return true;
            case KEY_HOME:
                this.moveCursorToStart();
                return true;
            case KEY_END:
                this.moveCursorToEnd();
                return true;
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean canConsumeInput() {
        return this.isVisible() && this.isFocused() && this.enabled;
    }

    public boolean charTyped(char typedChar, int mods) {
        if (!this.canConsumeInput()) return false;

        if (MinecraftStringUtil.isMinecraftAllowedCharacter(typedChar)) {
            if (this.enabled) {
                this.insertText(Character.toString(typedChar));
            }
            return true;
        }
        return false;
    }

    public boolean mousePressed(double mouseX, double mouseY, int mouseButton) {
        if (!this.isVisible()) return false;

        boolean mouseOnWidget = this.isMouseOnWidget(mouseX, mouseY);
        this.setFocused(mouseOnWidget);

        if (!mouseOnWidget && mouseButton == 0) return false;

        int i = ((int) mouseX) - this.x;
        if (this.drawsBackground) {
            i -= 4;
        }

        this.frame = 0;
        String s = getDefaultFont().trimToWidth(this.text.substring(this.displayPos), this.getInnerWidth());
        this.moveCursorTo(getDefaultFont().trimToWidth(s, i).length() + this.displayPos);
        return true;
    }

    public void drawComponent(DrawContextWrapper<?> drawContextWrapper) {
        if (!this.isVisible()) return;

        if (this.drawsBackground) {
            int borderColor = this.isFocused() ? FOCUSED_BORDER_COLOR : (this.hovered ? HOVERED_COLOR : NORMAL_BORDER_COLOR);
            drawContextWrapper.fillRect(this.x - 1, this.y - 1, this.x + this.width + 1, this.y + this.height + 1, borderColor);
            drawContextWrapper.fillRect(this.x, this.y, this.x + this.width, this.y + this.height, BACKGROUND_COLOR);
        }

        int i2 = this.textColor != null ? this.textColor : (this.enabled ? NORMAL_TEXT_COLOR : DISABLED_TEXT_COLOR);
        int j = this.cursorPos - this.displayPos;
        int k = this.highlightPos - this.displayPos;
        String trimmed = getDefaultFont().trimToWidth(this.text.substring(this.displayPos), this.getInnerWidth());
        boolean flag = j >= 0 && j <= trimmed.length();
        boolean flag1 = this.isFocused() && this.frame / 6 % 2 == 0 && flag;
        int l = this.drawsBackground ? this.x + 4 : this.x;
        int i1 = this.drawsBackground ? this.y + (this.height - 8) / 2 : this.y;
        int j1 = l;
        if (k > trimmed.length()) {
            k = trimmed.length();
        }

        if (!trimmed.isEmpty()) {
            String s1 = flag ? trimmed.substring(0, j) : trimmed;
            TextWrapper text = this.renderTextProvider.apply(s1, this.displayPos);
            j1 = drawContextWrapper.drawTextWithShadow(getDefaultFont(), text, (float)l, (float)i1, i2);
        }

        boolean flag2 = this.cursorPos < this.text.length() || this.text.length() >= this.maxStringLength;
        int k1 = j1;
        if (!flag) {
            k1 = j > 0 ? l + this.width : l;
        } else if (flag2) {
            k1 = j1 - 1;
            --j1;
        }

        if (!trimmed.isEmpty() && flag && j < trimmed.length()) {
            TextWrapper text = this.renderTextProvider.apply(trimmed.substring(j), this.cursorPos);
            drawContextWrapper.drawTextWithShadow(getDefaultFont(), text, (float)j1, (float)i1, i2);
        }

        if (!flag2 && this.suggestion != null) {
            drawContextWrapper.drawTextWithShadow(getDefaultFont(), this.suggestion, k1 - 1, i1, 0xFF808080);
        }

        if (flag1) {
            if (flag2) {
                drawContextWrapper.fillRect(k1, i1 - 1, k1 + 1, i1 + 1 + 9, NORMAL_TEXT_COLOR);
            } else {
                drawContextWrapper.drawTextWithShadow(getDefaultFont(), "_", (float)k1, (float)i1, i2);
            }
        }

        if (k != j) {
            int l1 = l + getDefaultFont().getWidth(trimmed.substring(0, k));
            this.drawSelectionBox(drawContextWrapper, k1, i1 - 1, l1 - 1, i1 + 1 + 9);
        }
    }

    private void drawSelectionBox(DrawContextWrapper<?> drawContextWrapper, int startX, int startY, int endX, int endY) {
        if (startX < endX) {
            int i = startX;
            startX = endX;
            endX = i;
        }

        if (startY < endY) {
            int j = startY;
            startY = endY;
            endY = j;
        }

        if (endX > this.x + this.width) {
            endX = this.x + this.width;
        }

        if (startX > this.x + this.width) {
            startX = this.x + this.width;
        }

        drawContextWrapper.drawTextHighlight(startX, startY, endX, endY);
    }

    public void setMaxStringLength(int maxStringLength) {
        this.maxStringLength = maxStringLength;

        if (this.text.length() <= maxStringLength) return;
        this.text = this.text.substring(0, maxStringLength);
        this.onChanged(this.text);
    }

    public int getInnerWidth() {
        return this.drawsBackground ? this.width - 8 : this.width;
    }

}
