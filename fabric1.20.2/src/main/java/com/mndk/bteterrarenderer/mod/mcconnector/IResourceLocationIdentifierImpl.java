package com.mndk.bteterrarenderer.mod.mcconnector;

import com.mndk.bteterrarenderer.mcconnector.IResourceLocation;
import net.minecraft.util.Identifier;

public record IResourceLocationIdentifierImpl(Identifier delegate) implements IResourceLocation {}