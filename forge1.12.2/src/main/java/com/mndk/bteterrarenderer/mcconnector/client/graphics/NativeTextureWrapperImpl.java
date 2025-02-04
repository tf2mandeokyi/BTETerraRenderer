package com.mndk.bteterrarenderer.mcconnector.client.graphics;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import javax.annotation.Nonnull;

@RequiredArgsConstructor
public class NativeTextureWrapperImpl extends AbstractNativeTextureWrapper {
    @Nonnull public final Integer delegate;
    @Getter public final int width;
    @Getter public final int height;
}
