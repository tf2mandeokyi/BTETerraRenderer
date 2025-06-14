package com.mndk.bteterrarenderer.mcconnector.util;

import net.minecraft.util.Identifier;

import javax.annotation.Nonnull;

public record ResourceLocationWrapperImpl(@Nonnull Identifier delegate) implements ResourceLocationWrapper {}