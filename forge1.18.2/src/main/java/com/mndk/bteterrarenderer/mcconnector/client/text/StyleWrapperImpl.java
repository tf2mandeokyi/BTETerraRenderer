package com.mndk.bteterrarenderer.mcconnector.client.text;

import net.minecraft.network.chat.Style;

import javax.annotation.Nonnull;

public record StyleWrapperImpl(@Nonnull Style delegate) implements StyleWrapper {}