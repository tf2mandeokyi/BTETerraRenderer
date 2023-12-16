package com.mndk.bteterrarenderer.mixin.mcconnector.input;

import com.mndk.bteterrarenderer.mcconnector.input.IKeyBinding;
import com.mndk.bteterrarenderer.mcconnector.input.InputKey;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = IKeyBinding.class, remap = false)
public interface IKeyBindingMixin {
    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    static IKeyBinding registerInternal(String description, InputKey key, String category) {
        KeyBinding keyBinding = new KeyBinding(description, InputUtil.Type.KEYSYM, key.glfwKeyCode, category);
        KeyBindingHelper.registerKeyBinding(keyBinding);
        return keyBinding::wasPressed;
    }
}
