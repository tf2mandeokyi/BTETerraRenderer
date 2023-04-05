package com.mndk.bteterrarenderer.connector.minecraft;

import com.mndk.bteterrarenderer.connector.ImplFinder;

public interface MouseConnector {
    MouseConnector INSTANCE = ImplFinder.search(MouseConnector.class);

    int getEventX();
    int getEventDWheel();
}
