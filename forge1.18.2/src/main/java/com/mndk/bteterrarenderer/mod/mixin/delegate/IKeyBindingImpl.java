package com.mndk.bteterrarenderer.mod.mixin.delegate;

import com.mndk.bteterrarenderer.core.input.IKeyBinding;
import net.minecraft.client.KeyMapping;

public record IKeyBindingImpl(KeyMapping delegate) implements IKeyBinding {
    @Override
    public boolean wasPressed() {
        return delegate.consumeClick();
    }
}
