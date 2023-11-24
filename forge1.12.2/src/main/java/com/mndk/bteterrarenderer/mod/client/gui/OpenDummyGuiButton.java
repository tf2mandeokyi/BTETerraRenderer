package com.mndk.bteterrarenderer.mod.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;

public class OpenDummyGuiButton extends GuiButton {
    public static final ResourceLocation WIDGET_TEXTURES = BUTTON_TEXTURES;

    public OpenDummyGuiButton() {
        super(0, 0, 0, "");
        throw new UnsupportedOperationException("Not allowed to use this class as a button!");
    }
}
