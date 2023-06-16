package com.mndk.bteterrarenderer.connector;

import com.mndk.bteterrarenderer.connector.gui.*;
import com.mndk.bteterrarenderer.connector.minecraft.IResourceLocation;

public interface DependencyConnectorSupplier {
    DependencyConnectorSupplier INSTANCE = ImplFinder.search();

    // Minecraft
    IGuiChat newGuiChat();
    IResourceLocation newResourceLocation(String modId, String location);
}