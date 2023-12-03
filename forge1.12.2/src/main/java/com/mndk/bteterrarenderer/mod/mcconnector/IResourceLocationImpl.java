package com.mndk.bteterrarenderer.mod.mcconnector;

import com.mndk.bteterrarenderer.mcconnector.IResourceLocation;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.minecraft.util.ResourceLocation;

@RequiredArgsConstructor @Getter @Setter
public class IResourceLocationImpl implements IResourceLocation {
    private final ResourceLocation delegate;
}
