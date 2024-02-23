package com.mndk.bteterrarenderer.mcconnector.client.input;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.input.Keyboard;

public class GameInputManagerImpl implements GameInputManager {

    public boolean isKeyDown(InputKey key) {
        return Keyboard.isKeyDown(key.keyboardCode);
    }

    public IKeyBinding registerInternal(String description, InputKey key, String category) {
        KeyBinding keyBinding = new KeyBinding(description, key.keyboardCode, category);
        ClientRegistry.registerKeyBinding(keyBinding);
        return keyBinding::isPressed;
    }

    public String getClipboardContent() {
        return GuiScreen.getClipboardString();
    }

    public void setClipboardContent(String content) {
        GuiScreen.setClipboardString(content);
    }
}
