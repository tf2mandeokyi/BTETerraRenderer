package com.mndk.bteterrarenderer.mod.network;

import com.mndk.bteterrarenderer.core.projection.Projections;
import com.mndk.bteterrarenderer.core.util.Loggers;
import com.mndk.bteterrarenderer.dep.terraplusplus.projection.GeographicProjection;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class ServerWelcomeMsgHandler implements IMessageHandler<ServerWelcomeMessageImpl, IMessage> {
    @Override
    public IMessage onMessage(ServerWelcomeMessageImpl message, MessageContext ctx) {
        GeographicProjection proj = message.getProjection();
        Loggers.get(this).info("Received GeographicProjection from the server side: " +
                message.getProjectionJson());
        Projections.setHologramProjection(proj);
        return null;
    }
}
