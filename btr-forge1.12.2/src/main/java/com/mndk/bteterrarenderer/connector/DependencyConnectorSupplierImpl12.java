package com.mndk.bteterrarenderer.connector;

import com.mndk.bteterrarenderer.connector.gui.IGuiChat;
import com.mndk.bteterrarenderer.connector.gui.IGuiChatImpl12;
import com.mndk.bteterrarenderer.connector.minecraft.IResourceLocation;
import com.mndk.bteterrarenderer.connector.minecraft.IResourceLocationImpl12;
import net.minecraft.util.ResourceLocation;

@ConnectorImpl
@SuppressWarnings("unused")
public class DependencyConnectorSupplierImpl12 implements DependencyConnectorSupplier {

    public IGuiChat newGuiChat() {
        return new IGuiChatImpl12();
    }

    public IResourceLocation newResourceLocation(String modId, String location) {
        return new IResourceLocationImpl12(new ResourceLocation(modId, location));
    }
}