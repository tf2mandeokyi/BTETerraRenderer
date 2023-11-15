package com.mndk.bteterrarenderer.mod.mixin.delegate;

import com.mndk.bteterrarenderer.core.input.IKeyBinding;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.settings.KeyBinding;

@RequiredArgsConstructor
public class IKeyBindingImpl implements IKeyBinding {
    private final KeyBinding delegate;
    @Override
    public boolean wasPressed() {
        return delegate.isPressed();
    }
}
