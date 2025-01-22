package com.mndk.bteterrarenderer.mcconnector.util;

import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

public record ResourceLocationWrapperImpl(@Nonnull ResourceLocation delegate) implements ResourceLocationWrapper {}