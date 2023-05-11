package com.mndk.bteterrarenderer.connector.minecraft;

import net.minecraft.resources.ResourceLocation;

public record IResourceLocationImpl(ResourceLocation delegate) implements IResourceLocation {}
