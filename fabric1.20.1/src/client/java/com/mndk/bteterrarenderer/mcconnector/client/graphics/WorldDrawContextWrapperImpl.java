package com.mndk.bteterrarenderer.mcconnector.client.graphics;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;

import javax.annotation.Nonnull;

public record WorldDrawContextWrapperImpl(
        @Nonnull MatrixStack stack,
        @Nonnull VertexConsumerProvider provider
) implements WorldDrawContextWrapper {}
