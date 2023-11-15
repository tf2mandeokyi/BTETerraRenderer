package com.mndk.bteterrarenderer.core.event;

import com.mndk.bteterrarenderer.core.loader.ConfigLoaders;
import com.mndk.bteterrarenderer.core.projection.Projections;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ClientConnectionEvents {
    public void onJoin() {
        Projections.setDefaultBTEProjection();
        ConfigLoaders.loadAll(true);
    }
}
