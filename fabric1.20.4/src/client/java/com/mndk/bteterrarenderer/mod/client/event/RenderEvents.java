package com.mndk.bteterrarenderer.mod.client.event;

import com.mndk.bteterrarenderer.core.tile.TileRenderer;
import com.mndk.bteterrarenderer.mcconnector.wrapper.DrawContextWrapper;
import lombok.experimental.UtilityClass;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Matrix4f;

@UtilityClass
public class RenderEvents {

    private static final VertexConsumerProvider.Immediate DUMMY_CONSUMER = VertexConsumerProvider.immediate(new BufferBuilder(0));

    public void registerEvents() {
        WorldRenderEvents.END.register(RenderEvents::onRender);
    }

    @SuppressWarnings("resource")
    public void onRender(WorldRenderContext renderContext) {
        World world = renderContext.world();
        MinecraftClient client = renderContext.gameRenderer().getClient();
        if(world == null) return;
        if(client.player == null) return;

        Matrix4f currentMatrix = renderContext.matrixStack().peek().getPositionMatrix();
        DrawContext context = new DrawContext(client, DUMMY_CONSUMER);
        context.getMatrices().multiplyPositionMatrix(currentMatrix);

        // While the player is the "rendering center" in 1.12.2,
        // After 1.18.2 it is the camera being that center.
        // So the camera's position should be given instead, unlike in 1.12.2.
        Vec3d cameraPos = renderContext.camera().getPos();
        world.getProfiler().swap("bteterrarenderer-hologram");
        TileRenderer.renderTiles(DrawContextWrapper.of(context), cameraPos.x, cameraPos.y, cameraPos.z);
    }
}
