package com.mndk.bteterrarenderer.connector.minecraft;

import com.mndk.bteterrarenderer.connector.ImplFinder;

public interface ClientPlayerConnector {
    ClientPlayerConnector INSTANCE = ImplFinder.search();

    double getRotationYaw();
}
