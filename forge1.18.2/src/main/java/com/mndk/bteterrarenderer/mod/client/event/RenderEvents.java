package com.mndk.bteterrarenderer.mod.client.event;

import com.mndk.bteterrarenderer.BTETerraRenderer;
import com.mndk.bteterrarenderer.core.tile.RenderManager;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.*;
import com.mojang.blaze3d.vertex.PoseStack;
import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@UtilityClass
@Mod.EventBusSubscriber(modid = BTETerraRenderer.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class RenderEvents {

    public void onWorldRender(final LevelRenderContext event) {
        ClientLevel world = event.getLevel();
        Minecraft mc = event.getGameRenderer().getMinecraft();
        if (world == null) return;
        if (mc.player == null) return;

        PoseStack stack = event.getPoseStack();
        MultiBufferSource provider = event.getMultiBufferSource();
        if (stack == null) return;
        WorldDrawContextWrapper context = new WorldDrawContextWrapperImpl(stack, provider);

        // While the player is the "rendering center" in 1.12.2,
        // After 1.18.2 it is the camera being that center.
        // So the camera's position should be given instead, unlike in 1.12.2.
        final Vec3 cameraPos = event.getCamera().getPosition();
        world.getProfiler().popPush("bteterrarenderer-hologram");
        RenderManager.renderTiles(context, cameraPos.x, cameraPos.y, cameraPos.z);
    }

    @SubscribeEvent
    public void onHudRender(final RenderGameOverlayEvent.Post event) {
        GuiDrawContextWrapper wrapper = new GuiDrawContextWrapperImpl(event.getMatrixStack());
        RenderManager.renderHud(wrapper);
    }
}
