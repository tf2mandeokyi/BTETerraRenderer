package com.mndk.bteterrarenderer.mcconnector.client.text;

import net.minecraft.text.Style;

import javax.annotation.Nonnull;

public record StyleWrapperImpl(@Nonnull Style delegate) implements StyleWrapper {}