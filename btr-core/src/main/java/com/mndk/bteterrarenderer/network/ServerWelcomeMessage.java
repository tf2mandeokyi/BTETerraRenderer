package com.mndk.bteterrarenderer.network;

import com.mndk.bteterrarenderer.connector.DependencyConnectorSupplier;
import com.mndk.bteterrarenderer.connector.netty.IByteBuf;
import com.mndk.bteterrarenderer.connector.terraplusplus.projection.IGeographicProjection;
import lombok.Getter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ServerWelcomeMessage {
    @Getter
    private String projectionJson = null;

    public ServerWelcomeMessage() {}

    public ServerWelcomeMessage(String projectionJson) {
        this.projectionJson = projectionJson;
    }

    public ServerWelcomeMessage(IGeographicProjection serverProjection) throws IOException {
        this(DependencyConnectorSupplier.INSTANCE.projectionToJson(serverProjection));
    }

    public void fromBytes(IByteBuf buf) {
        int strLength = buf.readInt();
        if(strLength != -1) {
            projectionJson = buf.readCharSequence(strLength, StandardCharsets.UTF_8).toString();
        }
    }

    public void toBytes(IByteBuf buf) {
        if(projectionJson != null) {
            buf.writeInt(projectionJson.getBytes(StandardCharsets.UTF_8).length);
            buf.writeCharSequence(projectionJson, StandardCharsets.UTF_8);
        } else {
            buf.writeInt(-1);
        }
    }

    public IGeographicProjection getProjection() {
        return DependencyConnectorSupplier.INSTANCE.parse(projectionJson);
    }
}
