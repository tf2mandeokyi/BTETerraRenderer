package com.mndk.bteterrarenderer.mod.util.mixin.delegate;

import com.mndk.bteterrarenderer.core.util.mixin.delegate.IResourceLocation;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.minecraft.util.ResourceLocation;

@RequiredArgsConstructor @Getter @Setter
public class IResourceLocationImpl12 implements IResourceLocation {
    private final ResourceLocation delegate;
}
