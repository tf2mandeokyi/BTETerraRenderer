package com.mndk.bteterrarenderer.event;

import com.mndk.bteterrarenderer.BTETerraRendererConstants;
import com.mndk.bteterrarenderer.config.BTRConfigConnector;
import com.mndk.bteterrarenderer.projection.Projections;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = BTETerraRendererConstants.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {

    // TODO: move these key mappings into somewhere like com.mndk.bteterrarenderer.client.KeyMappings
    public static KeyMapping mapToggleKey, mapOptionsKey;

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        mapToggleKey = registerKey("toggle", "category", InputConstants.KEY_R);
        mapOptionsKey = registerKey("options_ui", "category", InputConstants.KEY_GRAVE);

        Projections.setDefaultBTEProjection(); // TODO: Implement server-client connection rather than this
        BTRConfigConnector.refreshTileMapService();
    }

    private static KeyMapping registerKey(String name, String category, int keyCode) {
        KeyMapping key = new KeyMapping("key." + BTETerraRendererConstants.MODID + "." + name, keyCode, category);
        ClientRegistry.registerKeyBinding(key);
        return key;
    }

}
