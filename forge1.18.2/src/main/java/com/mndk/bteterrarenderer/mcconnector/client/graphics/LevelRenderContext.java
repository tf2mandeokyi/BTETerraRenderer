package com.mndk.bteterrarenderer.mcconnector.client.graphics;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;

@Getter
public class LevelRenderContext {

    private LevelRenderer levelRenderer;
    private PoseStack poseStack;
    private Camera camera;
    private GameRenderer gameRenderer;
    private MultiBufferSource multiBufferSource;
    private ClientLevel level;

    public void prepare(LevelRenderer levelRenderer, PoseStack poseStack, Camera camera, GameRenderer gameRenderer, MultiBufferSource multiBufferSource, ClientLevel level) {
        this.levelRenderer = levelRenderer;
        this.poseStack = poseStack;
        this.camera = camera;
        this.gameRenderer = gameRenderer;
        this.multiBufferSource = multiBufferSource;
        this.level = level;
    }

}
