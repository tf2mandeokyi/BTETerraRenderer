package com.mndk.bteterrarenderer.mcconnector.gui.component;

import com.mndk.bteterrarenderer.core.util.BTRUtil;
import com.mndk.bteterrarenderer.mcconnector.gui.FontRenderer;
import com.mndk.bteterrarenderer.mcconnector.gui.RawGuiManager;
import com.mndk.bteterrarenderer.mcconnector.input.GameInputManager;
import com.mndk.bteterrarenderer.mcconnector.input.InputKey;
import com.mndk.bteterrarenderer.mcconnector.util.MinecraftStringUtil;
import com.mndk.bteterrarenderer.mcconnector.wrapper.DrawContextWrapper;
import lombok.Getter;
import lombok.Setter;

import java.util.function.Predicate;

/**
 * Copied from both 1.12.2's <code>net.minecraft.client.gui.GuiTextField</code>
 * and 1.18.2's <code>net.minecraft.client.gui.components.EditBox</code>
 */
public class TextFieldWidgetCopy extends AbstractWidgetCopy {

    private static final int BACKGROUND_COLOR = 0xFF000000;

    @Setter
    private Integer textColor;
    @Setter
    private int maxStringLength = 32;
    private int frame;
    @Setter
    private boolean drawsBackground = true;
    private boolean shiftPressed;
    @Getter @Setter
    private int displayPos, cursorPos, highlightPos;
    @Setter
    private Predicate<String> validator = s -> true;

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
        }
    }

    private void deleteText(int delta) {
        if (GameInputManager.isControlKeyDown()) {
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

        for(int k = 0; k < j; ++k) {
            if (!flag) {
                int l = this.text.length();
                i = this.text.indexOf(32, i);
                if (i == -1) {
                    i = l;
                } else {
                    while(p_94143_ && i < l && this.text.charAt(i) == ' ') {
                        ++i;
                    }
                }
            } else {
                while(p_94143_ && i > 0 && this.text.charAt(i - 1) == ' ') {
                    --i;
                }

                while(i > 0 && this.text.charAt(i - 1) != ' ') {
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

    public boolean keyPressed(InputKey key) {
        if (!this.canConsumeInput()) return false;

        this.shiftPressed = GameInputManager.isShiftKeyDown();
        if (GameInputManager.isKeySelectAll(key)) {
            this.moveCursorToEnd();
            this.setHighlightPos(0);
            return true;
        } else if (GameInputManager.isKeyCopy(key)) {
            GameInputManager.setClipboardContent(this.getHighlighted());
            return true;
        } else if (GameInputManager.isKeyPaste(key)) {
            if (this.enabled) {
                this.insertText(GameInputManager.getClipboardContent());
            }
            return true;
        } else if (GameInputManager.isKeyCut(key)) {
            GameInputManager.setClipboardContent(this.getHighlighted());
            if (this.enabled) {
                this.insertText("");
            }
            return true;
        } else switch (key) {
            case KEY_BACKSPACE:
                if (this.enabled) {
                    this.shiftPressed = false;
                    this.deleteText(-1);
                    this.shiftPressed = GameInputManager.isShiftKeyDown();
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
                    this.shiftPressed = GameInputManager.isShiftKeyDown();
                }
                return true;
            case KEY_RIGHT:
                if (GameInputManager.isControlKeyDown()) {
                    this.moveCursorTo(this.getWordPosition(1));
                } else {
                    this.moveCursor(1);
                }
                return true;
            case KEY_LEFT:
                if (GameInputManager.isControlKeyDown()) {
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

    public boolean keyTyped(char typedChar, int mods) {
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
        String s = FontRenderer.DEFAULT.trimStringToWidth(this.text.substring(this.displayPos), this.getInnerWidth());
        this.moveCursorTo(FontRenderer.DEFAULT.trimStringToWidth(s, i).length() + this.displayPos);
        return true;
    }

    public void drawComponent(DrawContextWrapper drawContextWrapper) {
        if (!this.isVisible()) return;

        if (this.drawsBackground) {
            int borderColor = this.isFocused() ? FOCUSED_BORDER_COLOR : (this.hovered ? HOVERED_COLOR : NORMAL_BORDER_COLOR);
            RawGuiManager.INSTANCE.fillRect(drawContextWrapper, this.x - 1, this.y - 1, this.x + this.width + 1, this.y + this.height + 1, borderColor);
            RawGuiManager.INSTANCE.fillRect(drawContextWrapper, this.x, this.y, this.x + this.width, this.y + this.height, BACKGROUND_COLOR);
        }

        int i2 = this.textColor != null ? this.textColor : (this.enabled ? NORMAL_TEXT_COLOR : DISABLED_TEXT_COLOR);
        int j = this.cursorPos - this.displayPos;
        int k = this.highlightPos - this.displayPos;
        String trimmed = FontRenderer.DEFAULT.trimStringToWidth(this.text.substring(this.displayPos), this.getInnerWidth());
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
            j1 = FontRenderer.DEFAULT.drawStringWithShadow(drawContextWrapper, s1, (float)l, (float)i1, i2);
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
            FontRenderer.DEFAULT.drawStringWithShadow(drawContextWrapper, trimmed.substring(j), (float)j1, (float)i1, i2);
        }

        if (flag1) {
            if (flag2) {
                RawGuiManager.INSTANCE.fillRect(drawContextWrapper, k1, i1 - 1, k1 + 1, i1 + 1 + 9, NORMAL_TEXT_COLOR);
            } else {
                FontRenderer.DEFAULT.drawStringWithShadow(drawContextWrapper, "_", (float)k1, (float)i1, i2);
            }
        }

        if (k != j) {
            int l1 = l + FontRenderer.DEFAULT.getStringWidth(trimmed.substring(0, k));
            this.drawSelectionBox(drawContextWrapper, k1, i1 - 1, l1 - 1, i1 + 1 + 9);
        }
    }

    private void drawSelectionBox(DrawContextWrapper drawContextWrapper, int startX, int startY, int endX, int endY) {
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

        RawGuiManager.INSTANCE.drawTextFieldHighlight(drawContextWrapper, startX, startY, endX, endY);
    }

    public int getInnerWidth() {
        return this.drawsBackground ? this.width - 8 : this.width;
    }
}
