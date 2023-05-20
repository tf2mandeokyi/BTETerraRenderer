package com.mndk.bteterrarenderer.connector.minecraft;

import com.mndk.bteterrarenderer.connector.ImplFinder;

public interface MinecraftClientConnector {
    MinecraftClientConnector INSTANCE = ImplFinder.search();

    double getPlayerRotationYaw();

    void sendErrorMessageToChat(String message);
    void sendErrorMessageToChat(String message, Throwable t);

    void playClickSound();

    /*
     * Consider changing these to enum type
     */
    int chatOpenKeyCode();
    int commandOpenKeyCode();
}
