package com.mndk.bteterrarenderer.mod.util.mixin.delegate;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;

public class OpenDummyGuiButton12 extends GuiButton {
    public static final ResourceLocation WIDGET_TEXTURES = BUTTON_TEXTURES;

    public OpenDummyGuiButton12(int buttonId, int x, int y, String buttonText) {
        super(0, 0, 0, "");
        throw new UnsupportedOperationException("Not allowed to use this class as a button!");
    }
}
