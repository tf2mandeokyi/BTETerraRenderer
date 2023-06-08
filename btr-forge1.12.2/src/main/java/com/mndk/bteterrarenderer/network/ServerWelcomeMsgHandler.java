package com.mndk.bteterrarenderer.network;

import com.mndk.bteterrarenderer.BTETerraRendererConstants;
import com.mndk.bteterrarenderer.dep.terraplusplus.projection.GeographicProjection;
import com.mndk.bteterrarenderer.projection.Projections;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class ServerWelcomeMsgHandler implements IMessageHandler<ServerWelcomeMessageImpl12, IMessage> {
    @Override
    public IMessage onMessage(ServerWelcomeMessageImpl12 message, MessageContext ctx) {
        GeographicProjection proj = message.getProjection();
        BTETerraRendererConstants.LOGGER.info("Received GeographicProjection from the server side: " +
                message.getProjectionJson());
        Projections.setServerProjection(proj);
        return null;
    }
}
