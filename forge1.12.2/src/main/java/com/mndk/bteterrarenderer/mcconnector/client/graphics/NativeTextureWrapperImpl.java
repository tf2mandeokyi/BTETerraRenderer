package com.mndk.bteterrarenderer.mcconnector.client.graphics;

import lombok.RequiredArgsConstructor;
import javax.annotation.Nonnull;

@RequiredArgsConstructor
public class NativeTextureWrapperImpl extends AbstractNativeTextureWrapper {
    @Nonnull public final Integer delegate;
}
