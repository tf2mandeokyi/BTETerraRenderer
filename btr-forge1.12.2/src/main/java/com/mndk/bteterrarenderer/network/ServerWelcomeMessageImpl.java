package com.mndk.bteterrarenderer.network;

import com.mndk.bteterrarenderer.connector.netty.IByteBufImpl;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class ServerWelcomeMessageImpl extends ServerWelcomeMessage implements IMessage {
    @Override
    public void fromBytes(ByteBuf buf) {
        fromBytes(new IByteBufImpl(buf));
    }

    @Override
    public void toBytes(ByteBuf buf) {
        toBytes(new IByteBufImpl(buf));
    }
}
