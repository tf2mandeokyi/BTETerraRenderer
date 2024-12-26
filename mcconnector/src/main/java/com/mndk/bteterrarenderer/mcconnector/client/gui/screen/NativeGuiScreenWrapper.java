package com.mndk.bteterrarenderer.mcconnector.client.gui.screen;

public interface NativeGuiScreenWrapper extends GuiScreenCopy {

    void onDisplayed();

    /**
     * @return Whether the native screen listens for not only {@link #charTyped} but also {@link #keyPressed}
     */
    boolean alsoListensForKeyPress();
}
