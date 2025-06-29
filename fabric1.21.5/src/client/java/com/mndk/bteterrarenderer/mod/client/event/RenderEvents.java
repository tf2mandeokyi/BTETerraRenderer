package com.mndk.bteterrarenderer.mod.client.event;

import com.mndk.bteterrarenderer.core.tile.RenderManager;
import com.mndk.bteterrarenderer.mcconnector.client.gui.GuiDrawContextWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.gui.GuiDrawContextWrapperImpl;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.WorldDrawContextWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.WorldDrawContextWrapperImpl;
import lombok.experimental.UtilityClass;
import net.fabricmc.fabric.api.client.rendering.v1.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.profiler.Profilers;
import net.minecraft.world.World;

@UtilityClass
public class RenderEvents {

    public void registerEvents() {
        WorldRenderEvents.AFTER_ENTITIES.register(RenderEvents::onWorldRender);
        HudLayerRegistrationCallback.EVENT.register(layeredDrawer -> layeredDrawer.attachLayerAfter(
                IdentifiedLayer.MISC_OVERLAYS,
                Identifier.of("bteterrarenderer", "tile_renderer_info_layer"),
                RenderEvents::onHudRender
        ));
    }

    @SuppressWarnings("resource")
    public void onWorldRender(WorldRenderContext renderContext) {
        World world = renderContext.world();
        MinecraftClient client = renderContext.gameRenderer().getClient();
        if (world == null) return;
        if (client.player == null) return;

        MatrixStack stack = renderContext.matrixStack();
        VertexConsumerProvider provider = renderContext.consumers();
        if (stack == null) return;
        if (provider == null) return;
        WorldDrawContextWrapper context = new WorldDrawContextWrapperImpl(stack, provider);

        // While the player is the "rendering center" in 1.12.2,
        // After 1.18.2 it is the camera being that center.
        // So the camera's position should be given instead, unlike in 1.12.2.
        Vec3d cameraPos = renderContext.camera().getPos();
        Profilers.get().swap("bteterrarenderer-hologram");
        RenderManager.renderTiles(context, cameraPos.x, cameraPos.y, cameraPos.z);
    }

    public void onHudRender(DrawContext drawContext, RenderTickCounter tickCounter) {
        GuiDrawContextWrapper wrapper = new GuiDrawContextWrapperImpl(drawContext);
        RenderManager.renderHud(wrapper);
    }
}
