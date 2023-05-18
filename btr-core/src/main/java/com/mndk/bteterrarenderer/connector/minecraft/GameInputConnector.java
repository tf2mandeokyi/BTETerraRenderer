package com.mndk.bteterrarenderer.connector.minecraft;

import com.mndk.bteterrarenderer.connector.ImplFinder;

import static com.mndk.bteterrarenderer.connector.minecraft.InputKey.*;

public interface GameInputConnector {
    GameInputConnector INSTANCE = ImplFinder.search();

    boolean isOnMac();
    boolean isKeyDown(InputKey key);
    String getClipboardContent();
    void setClipboardContent(String content);

    default boolean isControlKeyDown() {
        if (isOnMac()) {
            return isKeyDown(KEY_LEFT_SUPER) || isKeyDown(KEY_RIGHT_SUPER);
        } else {
            return isKeyDown(KEY_LEFT_CONTROL) || isKeyDown(KEY_RIGHT_CONTROL);
        }
    }

    default boolean isShiftKeyDown() {
        return isKeyDown(KEY_LEFT_SHIFT) || isKeyDown(KEY_RIGHT_SHIFT);
    }

    default boolean isAltKeyDown() {
        return isKeyDown(KEY_LEFT_ALT) || isKeyDown(KEY_RIGHT_ALT);
    }

    default boolean isKeyCut(InputKey key) {
        return KEY_X == key && isControlKeyDown() && !isShiftKeyDown() && !isAltKeyDown();
    }

    default boolean isKeyPaste(InputKey key) {
        return KEY_V == key && isControlKeyDown() && !isShiftKeyDown() && !isAltKeyDown();
    }

    default boolean isKeyCopy(InputKey key) {
        return KEY_C == key && isControlKeyDown() && !isShiftKeyDown() && !isAltKeyDown();
    }

    default boolean isKeySelectAll(InputKey key) {
        return KEY_A == key && isControlKeyDown() && !isShiftKeyDown() && !isAltKeyDown();
    }

}
