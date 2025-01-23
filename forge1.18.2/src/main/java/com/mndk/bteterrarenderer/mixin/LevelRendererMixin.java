package com.mndk.bteterrarenderer.mixin;

import com.mndk.bteterrarenderer.mcconnector.client.graphics.LevelRenderContext;
import com.mndk.bteterrarenderer.mod.client.event.RenderEvents;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderBuffers;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @Shadow @Final private RenderBuffers renderBuffers;
    @Shadow private ClientLevel level;
    @Unique private final LevelRenderContext bTETerraRenderer$levelRenderContext = new LevelRenderContext();

    @Inject(method = "renderLevel", at = @At("HEAD"))
    private void beforeRender(PoseStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f matrix4f, CallbackInfo ci) {
        bTETerraRenderer$levelRenderContext.prepare((LevelRenderer) (Object) this, matrices, camera, gameRenderer, renderBuffers.bufferSource(), level);
    }

    @Inject(method = "renderLevel", at = @At(value = "CONSTANT", args = "stringValue=blockentities", ordinal = 0))
    private void afterEntities(CallbackInfo ci) {
        // I had to imitate fabric api's WorldRenderContext because otherwise
        // there would be rendering artifacts due to some quirky rendering behavior.
        RenderEvents.onWorldRender(bTETerraRenderer$levelRenderContext);
    }

}
