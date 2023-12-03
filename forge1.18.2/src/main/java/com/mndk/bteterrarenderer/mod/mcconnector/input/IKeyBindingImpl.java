package com.mndk.bteterrarenderer.mod.mcconnector.input;

import com.mndk.bteterrarenderer.mcconnector.input.IKeyBinding;
import net.minecraft.client.KeyMapping;

public record IKeyBindingImpl(KeyMapping delegate) implements IKeyBinding {
    @Override
    public boolean wasPressed() {
        return delegate.consumeClick();
    }
}
