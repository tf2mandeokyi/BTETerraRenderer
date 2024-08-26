package com.mndk.bteterrarenderer.draco.compression.attributes.scheme;

import com.mndk.bteterrarenderer.draco.core.DecoderBuffer;
import com.mndk.bteterrarenderer.draco.core.Status;

public interface PSchemeDecoderInterface extends PSchemeInterface {
    Status decodePredictionData(DecoderBuffer buffer);
}
