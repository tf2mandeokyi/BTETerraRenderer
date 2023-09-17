package com.mndk.bteterrarenderer.mod.util.mixin.delegate;

import com.mndk.bteterrarenderer.core.util.mixin.delegate.IResourceLocation;
import net.minecraft.resources.ResourceLocation;

public record IResourceLocationImpl18(ResourceLocation delegate) implements IResourceLocation {}
