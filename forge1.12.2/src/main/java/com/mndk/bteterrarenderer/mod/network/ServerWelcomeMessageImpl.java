package com.mndk.bteterrarenderer.mod.network;

import com.mndk.bteterrarenderer.core.network.ServerWelcomeMessage;
import com.mndk.bteterrarenderer.dep.terraplusplus.projection.GeographicProjection;
import net.buildtheearth.terraplusplus.TerraConstants;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.io.IOException;

public class ServerWelcomeMessageImpl extends ServerWelcomeMessage implements IMessage {

    public ServerWelcomeMessageImpl() {
        super();
    }

    public ServerWelcomeMessageImpl(net.buildtheearth.terraplusplus.projection.GeographicProjection bteProjection) throws IOException {
        super(GeographicProjection.parse(TerraConstants.JSON_MAPPER.writeValueAsString(bteProjection)));
    }

}
