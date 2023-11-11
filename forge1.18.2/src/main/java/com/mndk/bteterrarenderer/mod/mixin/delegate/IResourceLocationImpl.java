package com.mndk.bteterrarenderer.mod.mixin.delegate;

import com.mndk.bteterrarenderer.core.util.mixin.delegate.IResourceLocation;
import net.minecraft.resources.ResourceLocation;

public record IResourceLocationImpl(ResourceLocation delegate) implements IResourceLocation {}
