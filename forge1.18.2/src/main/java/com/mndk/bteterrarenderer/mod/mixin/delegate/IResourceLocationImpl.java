package com.mndk.bteterrarenderer.mod.mixin.delegate;

import com.mndk.bteterrarenderer.core.util.minecraft.IResourceLocation;
import net.minecraft.resources.ResourceLocation;

public record IResourceLocationImpl(ResourceLocation delegate) implements IResourceLocation {}
