package com.mndk.bteterrarenderer.mod.client.mixin.delegate;

import com.mndk.bteterrarenderer.core.input.IKeyBinding;
import net.minecraft.client.option.KeyBinding;

public record IKeyBindingImpl(KeyBinding delegate) implements IKeyBinding {
    @Override
    public boolean wasPressed() {
        return delegate.wasPressed();
    }
}
