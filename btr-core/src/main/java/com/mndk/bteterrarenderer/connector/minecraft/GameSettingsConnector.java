package com.mndk.bteterrarenderer.connector.minecraft;

import com.mndk.bteterrarenderer.connector.ImplFinder;

public interface GameSettingsConnector {
    GameSettingsConnector INSTANCE = ImplFinder.search(GameSettingsConnector.class);

    int getKeyBindChatCode();
    int getKeyBindCommandCode();
}
