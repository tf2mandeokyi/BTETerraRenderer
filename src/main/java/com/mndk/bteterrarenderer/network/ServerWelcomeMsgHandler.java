package com.mndk.bteterrarenderer.network;

import com.mndk.bteterrarenderer.BTETerraRenderer;
import com.mndk.bteterrarenderer.projection.Projections;
import net.buildtheearth.terraplusplus.projection.GeographicProjection;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class ServerWelcomeMsgHandler implements IMessageHandler<ServerWelcomeMessage, IMessage> {
    @Override
    public IMessage onMessage(ServerWelcomeMessage message, MessageContext ctx) {
        GeographicProjection proj = message.getProjection();
        BTETerraRenderer.logger.info("Received GeographicProjection from the server side: " +
                message.getProjectionJson());
        Projections.setServerProjection(proj);
        return null;
    }
}
