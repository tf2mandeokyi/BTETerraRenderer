package com.mndk.bteterrarenderer.mod.mixin.delegate;

import com.mndk.bteterrarenderer.core.util.minecraft.IResourceLocation;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.minecraft.util.ResourceLocation;

@RequiredArgsConstructor @Getter @Setter
public class IResourceLocationImpl implements IResourceLocation {
    private final ResourceLocation delegate;
}
