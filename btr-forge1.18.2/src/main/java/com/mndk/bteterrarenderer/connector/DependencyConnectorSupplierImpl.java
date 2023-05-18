package com.mndk.bteterrarenderer.connector;

import com.mndk.bteterrarenderer.connector.gui.IGuiChat;
import com.mndk.bteterrarenderer.connector.minecraft.IResourceLocation;
import com.mndk.bteterrarenderer.connector.minecraft.IResourceLocationImpl;
import net.minecraft.resources.ResourceLocation;

@ConnectorImpl
@SuppressWarnings("unused")
public class DependencyConnectorSupplierImpl implements DependencyConnectorSupplier {

    @Override
    public IGuiChat newGuiChat() {
        // TODO: Implement this
        return null;
    }

    public IResourceLocation newResourceLocation(String modId, String location) {
        return new IResourceLocationImpl(new ResourceLocation(modId, location));
    }
}