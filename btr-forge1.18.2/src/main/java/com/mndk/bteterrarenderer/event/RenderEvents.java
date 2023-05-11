package com.mndk.bteterrarenderer.event;

import com.mndk.bteterrarenderer.BTETerraRendererConstants;
import com.mndk.bteterrarenderer.config.BTRConfigConnector;
import com.mndk.bteterrarenderer.connector.graphics.GraphicsConnectorImpl;
import com.mndk.bteterrarenderer.tile.TileRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = BTETerraRendererConstants.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class RenderEvents {
    @SubscribeEvent
    public static void onRenderEvent(final RenderLevelStageEvent event) {
        if(!event.getStage().equals(RenderLevelStageEvent.Stage.AFTER_WEATHER)) return;
        if(!BTRConfigConnector.INSTANCE.isDoRender()) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if(player == null) return;

        GraphicsConnectorImpl.POSE_STACK = new PoseStack();

        // While the player is the "rendering center" in 1.12.2,
        // In 1.18.8 the camera becomes that center instead.
        // So it's the camera's position that should be given to TileRenderer.renderTiles(), unlike in 1.12.2.
        final Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        TileRenderer.renderTiles(cameraPos.x, cameraPos.y, cameraPos.z);

        GraphicsConnectorImpl.POSE_STACK = null;
    }
}
