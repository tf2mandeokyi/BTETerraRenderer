package com.mndk.bteterrarenderer.mcconnector.client.graphics;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;

import javax.annotation.Nonnull;

public record WorldDrawContextWrapperImpl(
        @Nonnull PoseStack stack,
        @Nonnull MultiBufferSource provider
) implements WorldDrawContextWrapper {}
