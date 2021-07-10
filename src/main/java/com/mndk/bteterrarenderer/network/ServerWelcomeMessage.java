package com.mndk.bteterrarenderer.network;

import io.netty.buffer.ByteBuf;
import net.buildtheearth.terraplusplus.generator.EarthGeneratorSettings;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.nio.charset.StandardCharsets;

public class ServerWelcomeMessage implements IMessage {

    private String earthGeneratorSettings;

    public ServerWelcomeMessage() {}

    public ServerWelcomeMessage(String earthGeneratorSettings) {
        this.earthGeneratorSettings = earthGeneratorSettings;
    }

    public ServerWelcomeMessage(EarthGeneratorSettings earthGeneratorSettings) {
        this(earthGeneratorSettings.toString());
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int strLength = buf.readInt();
        earthGeneratorSettings = buf.readCharSequence(strLength, StandardCharsets.UTF_8).toString();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(earthGeneratorSettings.getBytes(StandardCharsets.UTF_8).length);
        buf.writeCharSequence(earthGeneratorSettings, StandardCharsets.UTF_8);
    }

    public EarthGeneratorSettings getEarthGeneratorSettings() {
        return EarthGeneratorSettings.parse(earthGeneratorSettings);
    }
}
