package com.mndk.bteterrarenderer.core.event;

import com.mndk.bteterrarenderer.core.config.BTETerraRendererConfig;
import com.mndk.bteterrarenderer.core.projection.Projections;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ClientConnectionEvents {
    public void onJoin() {
        Projections.setDefaultBTEProjection();
        BTETerraRendererConfig.load(false);
    }
    public void onLeave() {
        BTETerraRendererConfig.save();
    }
}
