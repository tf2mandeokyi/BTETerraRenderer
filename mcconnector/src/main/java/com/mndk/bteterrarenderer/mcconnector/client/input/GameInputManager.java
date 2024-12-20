package com.mndk.bteterrarenderer.mcconnector.client.input;

import com.mndk.bteterrarenderer.mcconnector.McConnector;

import static com.mndk.bteterrarenderer.mcconnector.client.input.InputKey.*;

public interface GameInputManager {

    boolean isKeyDown(InputKey key);

    IKeyBinding registerInternal(String description, InputKey key, String category);
    default IKeyBinding register(String modId, String name, InputKey key) {
        return this.registerInternal("key." + modId + "." + name, key, "key." + modId + ".category");
    }

    String getClipboardContent();
    void setClipboardContent(String content);

    default boolean isControlKeyDown() {
        if (McConnector.client().isOnMac()) {
            return isKeyDown(KEY_LEFT_SUPER) || isKeyDown(KEY_RIGHT_SUPER);
        } else {
            return isKeyDown(KEY_LEFT_CONTROL) || isKeyDown(KEY_RIGHT_CONTROL);
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    default boolean isShiftKeyDown() {
        return isKeyDown(KEY_LEFT_SHIFT) || isKeyDown(KEY_RIGHT_SHIFT);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
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
