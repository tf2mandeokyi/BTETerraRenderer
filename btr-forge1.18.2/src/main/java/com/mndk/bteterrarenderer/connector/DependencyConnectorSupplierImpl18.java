package com.mndk.bteterrarenderer.connector;

import com.mndk.bteterrarenderer.connector.gui.IGuiChat;
import com.mndk.bteterrarenderer.connector.minecraft.IResourceLocation;
import com.mndk.bteterrarenderer.connector.minecraft.IResourceLocationImpl18;
import net.minecraft.resources.ResourceLocation;

public class DependencyConnectorSupplierImpl18 implements DependencyConnectorSupplier {

    @Override
    public IGuiChat newGuiChat() {
        // TODO: Implement or delete this
        return null;
    }

    public IResourceLocation newResourceLocation(String modId, String location) {
        return new IResourceLocationImpl18(new ResourceLocation(modId, location));
    }
}