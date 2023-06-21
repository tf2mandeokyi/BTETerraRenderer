package com.mndk.bteterrarenderer.client.event;

import com.mndk.bteterrarenderer.BTETerraRendererConstants;
import com.mndk.bteterrarenderer.tile.TileRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = BTETerraRendererConstants.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class RenderEvent {
    @SubscribeEvent
    public static void onRenderEvent(final RenderLevelLastEvent event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if(player == null) return;

        // While the player is the "rendering center" in 1.12.2,
        // In 1.18.8 it is the camera being that center.
        // So the camera's position should be given instead to TileRenderer.renderTiles(), unlike in 1.12.2.
        final Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        Minecraft.getInstance().getProfiler().push("hologram_render");
        TileRenderer.renderTiles(event.getPoseStack(), cameraPos.x, cameraPos.y, cameraPos.z);
        Minecraft.getInstance().getProfiler().pop();
    }
}
