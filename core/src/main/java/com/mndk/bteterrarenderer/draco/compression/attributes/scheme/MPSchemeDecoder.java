package com.mndk.bteterrarenderer.draco.compression.attributes.scheme;

import com.mndk.bteterrarenderer.draco.attributes.PointAttribute;
import lombok.AccessLevel;
import lombok.Getter;

public abstract class MPSchemeDecoder<DataT, CorrT> extends PSchemeDecoder<DataT, CorrT> {

    @Getter(AccessLevel.PROTECTED)
    private final MPSchemeData<?> meshData;

    public MPSchemeDecoder(PointAttribute attribute,
                           PSchemeDecodingTransform<DataT, CorrT> transform,
                           MPSchemeData<?> meshData) {
        super(attribute, transform);
        this.meshData = meshData;
    }
}
