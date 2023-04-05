package com.mndk.bteterrarenderer.network;

import com.mndk.bteterrarenderer.connector.netty.IByteBufImpl;
import com.mndk.bteterrarenderer.connector.terraplusplus.projection.IGeographicProjectionImpl;
import io.netty.buffer.ByteBuf;
import net.buildtheearth.terraplusplus.projection.GeographicProjection;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.io.IOException;

public class ServerWelcomeMessageImpl extends ServerWelcomeMessage implements IMessage {

    public ServerWelcomeMessageImpl() {
        super();
    }

    public ServerWelcomeMessageImpl(GeographicProjection serverProjection) throws IOException {
        super(new IGeographicProjectionImpl(serverProjection));
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        fromBytes(new IByteBufImpl(buf));
    }

    @Override
    public void toBytes(ByteBuf buf) {
        toBytes(new IByteBufImpl(buf));
    }
}
