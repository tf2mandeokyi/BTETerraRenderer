package com.mndk.bteterrarenderer.connector.minecraft;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.minecraft.util.ResourceLocation;

@RequiredArgsConstructor @Getter @Setter
public class IResourceLocationImpl implements IResourceLocation {
    private final ResourceLocation delegate;
}
