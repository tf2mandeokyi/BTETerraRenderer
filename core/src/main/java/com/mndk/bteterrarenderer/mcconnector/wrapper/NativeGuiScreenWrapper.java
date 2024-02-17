package com.mndk.bteterrarenderer.mcconnector.wrapper;

import com.mndk.bteterrarenderer.mcconnector.MixinUtil;
import com.mndk.bteterrarenderer.mcconnector.gui.screen.GuiScreenCopy;

import javax.annotation.Nonnull;

public abstract class NativeGuiScreenWrapper<T> extends MinecraftNativeObjectWrapper<T> implements GuiScreenCopy {

    public static NativeGuiScreenWrapper<?> of(@Nonnull Object delegate) {
        return MixinUtil.notOverwritten(delegate);
    }

    protected NativeGuiScreenWrapper(@Nonnull Object delegate) {
        super(delegate);
    }

    public abstract void onDisplayed();

    /**
     * @return Whether the native screen listens for not only {@link #charTyped} but also {@link #keyPressed}
     */
    public abstract boolean alsoListensForKeyPress();
}
