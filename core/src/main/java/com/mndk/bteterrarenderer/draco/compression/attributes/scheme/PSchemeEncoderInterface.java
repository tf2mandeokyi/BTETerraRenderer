package com.mndk.bteterrarenderer.draco.compression.attributes.scheme;

import com.mndk.bteterrarenderer.draco.core.EncoderBuffer;
import com.mndk.bteterrarenderer.draco.core.Status;

public interface PSchemeEncoderInterface extends PSchemeInterface {
    Status encodePredictionData(EncoderBuffer buffer);
}
