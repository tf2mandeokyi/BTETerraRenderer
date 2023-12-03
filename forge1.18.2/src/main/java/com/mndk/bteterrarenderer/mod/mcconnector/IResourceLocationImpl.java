package com.mndk.bteterrarenderer.mod.mcconnector;

import com.mndk.bteterrarenderer.mcconnector.IResourceLocation;
import net.minecraft.resources.ResourceLocation;

public record IResourceLocationImpl(ResourceLocation delegate) implements IResourceLocation {}
