package com.mndk.bteterrarenderer.draco.compression.attributes.scheme;

import com.mndk.bteterrarenderer.datatype.number.UShort;
import com.mndk.bteterrarenderer.draco.attributes.PointAttribute;
import com.mndk.bteterrarenderer.draco.compression.config.PredictionSchemeMethod;

public interface MPSchemeFactory<S, T> {
    S create(PredictionSchemeMethod method, PointAttribute attribute, T transform,
             MPSchemeData<?> data, UShort bitstreamVersion);
}
