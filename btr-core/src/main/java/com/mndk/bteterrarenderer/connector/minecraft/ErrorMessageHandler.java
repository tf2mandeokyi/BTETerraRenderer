package com.mndk.bteterrarenderer.connector.minecraft;

import com.mndk.bteterrarenderer.connector.ImplFinder;

public interface ErrorMessageHandler {
    ErrorMessageHandler INSTANCE = ImplFinder.search(ErrorMessageHandler.class);

    void sendToClient(String message);
    void sendToClient(String message, Throwable t);
}
