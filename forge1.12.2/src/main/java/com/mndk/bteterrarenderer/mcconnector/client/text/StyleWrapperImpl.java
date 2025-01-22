package com.mndk.bteterrarenderer.mcconnector.client.text;

import lombok.RequiredArgsConstructor;
import net.minecraft.util.text.Style;

import javax.annotation.Nonnull;

@RequiredArgsConstructor
public class StyleWrapperImpl implements StyleWrapper {
    @Nonnull public final Style delegate;
}