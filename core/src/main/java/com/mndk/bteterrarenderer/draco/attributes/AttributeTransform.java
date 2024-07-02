package com.mndk.bteterrarenderer.draco.attributes;

import com.mndk.bteterrarenderer.datatype.number.UByte;
import com.mndk.bteterrarenderer.draco.core.DecoderBuffer;
import com.mndk.bteterrarenderer.draco.core.DracoDataType;
import com.mndk.bteterrarenderer.draco.core.EncoderBuffer;
import com.mndk.bteterrarenderer.draco.core.Status;

import java.util.List;

/**
 * Virtual base class for various attribute transforms, enforcing common
 * interface where possible.
 */
public abstract class AttributeTransform {

    /** Return attribute transform type. */
    public abstract AttributeTransformType getType();
    /**
     * Try to init transform from attribute.
     */
    public abstract Status initFromAttribute(PointAttribute attribute);
    /** Copy parameter values into the provided AttributeTransformData instance. */
    public abstract void copyToAttributeTransformData(AttributeTransformData outData);

    public Status transferToAttribute(PointAttribute attribute) {
        AttributeTransformData transformData = new AttributeTransformData();
        this.copyToAttributeTransformData(transformData);
        attribute.setAttributeTransformData(transformData);
        return Status.ok();
    }

    /**
     * Applies the transform to {@code attribute} and stores the result in
     * {@code target_attribute}. {@code point_ids} is an optional vector that can be used to
     * remap values during the transform.
     */
    public abstract Status transformAttribute(
            PointAttribute attribute, List<PointIndex> pointIds, PointAttribute targetAttribute);

    /**
     * Applies an inverse transform to {@code attribute} and stores the result in
     * {@code target_attribute}. In this case, {@code attribute} is an attribute that was
     * already transformed (e.g. quantized) and {@code target_attribute} is the
     * attribute before the transformation.
     */
    public abstract Status inverseTransformAttribute(PointAttribute attribute, PointAttribute targetAttribute);

    /**
     * Encodes all data needed by the transformation into the {@code encoderBuffer}.
     */
    public abstract Status encodeParameters(EncoderBuffer encoderBuffer);

    /**
     * Decodes all data needed to transform {@code attribute} back to the original format.
     */
    public abstract Status decodeParameters(PointAttribute attribute, DecoderBuffer decoderBuffer);

    /**
     * Initializes a transformed attribute that can be used as target in the
     * {@link AttributeTransform#transformAttribute} function call.
     */
    public PointAttribute initTransformedAttribute(PointAttribute srcAttribute, int numEntries) {
        int numComponents = this.getTransformedNumComponents(srcAttribute);
        DracoDataType dataType = this.getTransformedDataType(srcAttribute);
        GeometryAttribute geometryAttribute = new GeometryAttribute();
        geometryAttribute.init(srcAttribute.getAttributeType(), null, UByte.of(numComponents), dataType,
                false, numComponents * dataType.getDataTypeLength(), 0);
        PointAttribute transformedAttribute = new PointAttribute(geometryAttribute);
        transformedAttribute.reset(numEntries);
        transformedAttribute.setIdentityMapping();
        transformedAttribute.setUniqueId(srcAttribute.getUniqueId());
        return transformedAttribute;
    }

    protected abstract DracoDataType getTransformedDataType(PointAttribute attribute);
    protected abstract int getTransformedNumComponents(PointAttribute attribute);
}
