package com.mndk.bteterrarenderer.connector;

import com.mndk.bteterrarenderer.connector.gui.IGuiChat;
import com.mndk.bteterrarenderer.connector.gui.IGuiChatImpl;
import com.mndk.bteterrarenderer.connector.minecraft.IResourceLocation;
import com.mndk.bteterrarenderer.connector.minecraft.IResourceLocationImpl;
import net.minecraft.util.ResourceLocation;

@ConnectorImpl
@SuppressWarnings("unused")
public class DependencyConnectorSupplierImpl implements DependencyConnectorSupplier {

    public IGuiChat newGuiChat() {
        return new IGuiChatImpl();
    }

    public IResourceLocation newResourceLocation(String modId, String location) {
        return new IResourceLocationImpl(new ResourceLocation(modId, location));
    }
}