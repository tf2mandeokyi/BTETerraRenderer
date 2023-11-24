package com.mndk.bteterrarenderer.mixin.input;

import com.mndk.bteterrarenderer.core.input.IKeyBinding;
import com.mndk.bteterrarenderer.core.input.InputKey;
import com.mndk.bteterrarenderer.mod.client.input.IKeyBindingImpl;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = IKeyBinding.class, remap = false)
public interface IKeyBindingMixin {
    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    static IKeyBinding registerInternal(String description, InputKey key, String category) {
        KeyBinding keyBinding = new KeyBinding(description, key.keyboardCode, category);
        ClientRegistry.registerKeyBinding(keyBinding);
        return new IKeyBindingImpl(keyBinding);
    }
}
