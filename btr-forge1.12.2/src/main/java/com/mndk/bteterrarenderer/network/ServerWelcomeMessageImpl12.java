package com.mndk.bteterrarenderer.network;

import com.mndk.bteterrarenderer.dep.terraplusplus.projection.GeographicProjection;
import net.buildtheearth.terraplusplus.TerraConstants;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.io.IOException;

public class ServerWelcomeMessageImpl12 extends ServerWelcomeMessage implements IMessage {

    public ServerWelcomeMessageImpl12() {
        super();
    }

    public ServerWelcomeMessageImpl12(net.buildtheearth.terraplusplus.projection.GeographicProjection bteProjection) throws IOException {
        super(GeographicProjection.parse(TerraConstants.JSON_MAPPER.writeValueAsString(bteProjection)));
    }

}
