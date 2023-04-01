package com.mndk.bteterrarenderer.network;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import net.buildtheearth.terraplusplus.TerraConstants;
import net.buildtheearth.terraplusplus.projection.GeographicProjection;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ServerWelcomeMessage {
    @Getter
    private String projectionJson = null;

    public ServerWelcomeMessage() {}

    public ServerWelcomeMessage(String projectionJson) {
        this.projectionJson = projectionJson;
    }

    public ServerWelcomeMessage(GeographicProjection serverProjection) throws IOException {
        this(TerraConstants.JSON_MAPPER.writeValueAsString(serverProjection));
    }

    public void fromBytes(ByteBuf buf) {
        int strLength = buf.readInt();
        if(strLength != -1) {
            projectionJson = buf.readCharSequence(strLength, StandardCharsets.UTF_8).toString();
        }
    }

    public void toBytes(ByteBuf buf) {
        if(projectionJson != null) {
            buf.writeInt(projectionJson.getBytes(StandardCharsets.UTF_8).length);
            buf.writeCharSequence(projectionJson, StandardCharsets.UTF_8);
        } else {
            buf.writeInt(-1);
        }
    }

    public GeographicProjection getProjection() {
        return GeographicProjection.parse(projectionJson);
    }
}
