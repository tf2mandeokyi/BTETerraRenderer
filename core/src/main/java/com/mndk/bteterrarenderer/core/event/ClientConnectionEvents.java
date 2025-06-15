package com.mndk.bteterrarenderer.core.event;

import com.mndk.bteterrarenderer.core.loader.LoaderRegistry;
import com.mndk.bteterrarenderer.core.projection.Projections;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ClientConnectionEvents {
    public void onJoin() {
        Projections.updateHologramProjection();
        LoaderRegistry.load(false);
    }
    public void onLeave() {
        LoaderRegistry.save();
    }
}
