package com.mndk.bteterrarenderer.mcconnector.client.text;

import com.mndk.bteterrarenderer.mcconnector.util.MinecraftObjectWrapper;
import net.minecraft.text.Style;

import javax.annotation.Nonnull;

public class StyleWrapperImpl extends MinecraftObjectWrapper<Style> implements StyleWrapper {
    protected StyleWrapperImpl(@Nonnull Style delegate) {
        super(delegate);
    }
}
