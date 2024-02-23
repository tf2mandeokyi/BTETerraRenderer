package com.mndk.bteterrarenderer.mcconnector.client.gui.screen;

import com.mndk.bteterrarenderer.mcconnector.util.MinecraftNativeObjectWrapper;

import javax.annotation.Nonnull;

public abstract class NativeGuiScreenWrapper<T> extends MinecraftNativeObjectWrapper<T> implements GuiScreenCopy {

    protected NativeGuiScreenWrapper(@Nonnull T delegate) {
        super(delegate);
    }

    public abstract void onDisplayed();

    /**
     * @return Whether the native screen listens for not only {@link #charTyped} but also {@link #keyPressed}
     */
    public abstract boolean alsoListensForKeyPress();
}
