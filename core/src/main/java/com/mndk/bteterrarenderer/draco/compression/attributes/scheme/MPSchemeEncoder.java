package com.mndk.bteterrarenderer.draco.compression.attributes.scheme;

import com.mndk.bteterrarenderer.draco.attributes.PointAttribute;
import lombok.AccessLevel;
import lombok.Getter;

public abstract class MPSchemeEncoder<DataT, CorrT> extends PSchemeEncoder<DataT, CorrT> {

    @Getter(AccessLevel.PROTECTED)
    private final MPSchemeData<?> meshData;

    public MPSchemeEncoder(PointAttribute attribute,
                           PSchemeEncodingTransform<DataT, CorrT> transform,
                           MPSchemeData<?> meshData) {
        super(attribute, transform);
        this.meshData = meshData;
    }
}
