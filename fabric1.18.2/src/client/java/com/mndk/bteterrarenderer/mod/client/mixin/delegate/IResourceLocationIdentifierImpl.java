package com.mndk.bteterrarenderer.mod.client.mixin.delegate;

import com.mndk.bteterrarenderer.core.util.mixin.delegate.IResourceLocation;
import net.minecraft.util.Identifier;

public record IResourceLocationIdentifierImpl(Identifier delegate) implements IResourceLocation {}