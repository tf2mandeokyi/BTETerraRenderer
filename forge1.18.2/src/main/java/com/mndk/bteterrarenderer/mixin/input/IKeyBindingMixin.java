package com.mndk.bteterrarenderer.mixin.input;

import com.mndk.bteterrarenderer.core.input.IKeyBinding;
import com.mndk.bteterrarenderer.core.input.InputKey;
import com.mndk.bteterrarenderer.mod.mixin.delegate.IKeyBindingImpl;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.ClientRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = IKeyBinding.class, remap = false)
public interface IKeyBindingMixin {
    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    static IKeyBinding registerInternal(String description, InputKey key, String category) {
        KeyMapping keyMapping = new KeyMapping(description, key.glfwKeyCode, category);
        ClientRegistry.registerKeyBinding(keyMapping);
        return new IKeyBindingImpl(keyMapping);
    }
}
