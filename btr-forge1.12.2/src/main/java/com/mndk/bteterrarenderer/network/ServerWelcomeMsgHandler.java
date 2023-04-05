package com.mndk.bteterrarenderer.network;

import com.mndk.bteterrarenderer.BTETerraRendererCore;
import com.mndk.bteterrarenderer.connector.terraplusplus.projection.IGeographicProjection;
import com.mndk.bteterrarenderer.projection.Projections;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class ServerWelcomeMsgHandler implements IMessageHandler<ServerWelcomeMessageImpl, IMessage> {
    @Override
    public IMessage onMessage(ServerWelcomeMessageImpl message, MessageContext ctx) {
        IGeographicProjection proj = message.getProjection();
        BTETerraRendererCore.logger.info("Received GeographicProjection from the server side: " +
                message.getProjectionJson());
        Projections.setServerProjection(proj);
        return null;
    }
}
