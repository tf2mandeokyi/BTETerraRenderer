package com.mndk.bteterrarenderer.connector.minecraft;

public interface ErrorMessageHandler {

    void sendToClient(String message);
    void sendToClient(String message, Throwable t);

}
