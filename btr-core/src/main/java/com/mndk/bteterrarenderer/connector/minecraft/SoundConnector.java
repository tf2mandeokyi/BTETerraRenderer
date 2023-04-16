package com.mndk.bteterrarenderer.connector.minecraft;

import com.mndk.bteterrarenderer.connector.ImplFinder;

public interface SoundConnector {
    SoundConnector INSTANCE = ImplFinder.search();

    void playClickSound();
}
