package com.mndk.bteterrarenderer.connector.minecraft;

import com.mndk.bteterrarenderer.connector.ImplFinder;

public interface KeyBindingsConnector {
    KeyBindingsConnector INSTANCE = ImplFinder.search();

    int chatOpenKeyCode();
    int commandOpenKeyCode();
}
