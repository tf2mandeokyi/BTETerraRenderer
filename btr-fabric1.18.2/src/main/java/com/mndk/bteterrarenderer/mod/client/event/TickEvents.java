package com.mndk.bteterrarenderer.mod.client.event;

import com.mndk.bteterrarenderer.core.config.BTETerraRendererConfig;
import com.mndk.bteterrarenderer.core.gui.MapRenderingOptionsSidebar;
import com.mndk.bteterrarenderer.mod.client.KeyBindings;
import lombok.experimental.UtilityClass;
import net.minecraft.client.MinecraftClient;

@UtilityClass
public class TickEvents {
    public void onStartTick(MinecraftClient ignored) {
        while(KeyBindings.MAP_TOGGLE_KEY.wasPressed()) {
//            BTETerraRendererConfigImpl.saveRenderState();
            BTETerraRendererConfig.INSTANCE.toggleRender();
        }
        if(KeyBindings.MAP_OPTIONS_KEY.wasPressed()) {
            MapRenderingOptionsSidebar.open();
        }
    }
}
