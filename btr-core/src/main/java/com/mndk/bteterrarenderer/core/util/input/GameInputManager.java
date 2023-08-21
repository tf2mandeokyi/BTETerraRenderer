package com.mndk.bteterrarenderer.core.util.input;

import com.mndk.bteterrarenderer.core.util.mixin.MixinUtil;
import lombok.experimental.UtilityClass;

import static com.mndk.bteterrarenderer.core.util.input.InputKey.*;

@UtilityClass
public class GameInputManager {

    public boolean isOnMac() {
        return MixinUtil.notOverwritten();
    }
    public boolean isKeyDown(InputKey key) {
        return MixinUtil.notOverwritten(key);
    }
    public String getClipboardContent() {
        return MixinUtil.notOverwritten();
    }
    public void setClipboardContent(String content) {
        MixinUtil.notOverwritten(content);
    }

    public boolean isControlKeyDown() {
        if (isOnMac()) {
            return isKeyDown(KEY_LEFT_SUPER) || isKeyDown(KEY_RIGHT_SUPER);
        } else {
            return isKeyDown(KEY_LEFT_CONTROL) || isKeyDown(KEY_RIGHT_CONTROL);
        }
    }

    public boolean isShiftKeyDown() {
        return isKeyDown(KEY_LEFT_SHIFT) || isKeyDown(KEY_RIGHT_SHIFT);
    }

    public boolean isAltKeyDown() {
        return isKeyDown(KEY_LEFT_ALT) || isKeyDown(KEY_RIGHT_ALT);
    }

    public boolean isKeyCut(InputKey key) {
        return KEY_X == key && isControlKeyDown() && !isShiftKeyDown() && !isAltKeyDown();
    }

    public boolean isKeyPaste(InputKey key) {
        return KEY_V == key && isControlKeyDown() && !isShiftKeyDown() && !isAltKeyDown();
    }

    public boolean isKeyCopy(InputKey key) {
        return KEY_C == key && isControlKeyDown() && !isShiftKeyDown() && !isAltKeyDown();
    }

    public boolean isKeySelectAll(InputKey key) {
        return KEY_A == key && isControlKeyDown() && !isShiftKeyDown() && !isAltKeyDown();
    }

}
