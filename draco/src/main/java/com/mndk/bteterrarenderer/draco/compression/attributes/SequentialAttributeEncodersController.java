/*
 * Copyright (C) 2024 The Draco Authors (for providing the original C++ code)
 * Copyright (C) 2024 m4ndeokyi (for translating the code into Java)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mndk.bteterrarenderer.draco.compression.attributes;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.UByte;
import com.mndk.bteterrarenderer.draco.attributes.GeometryAttribute;
import com.mndk.bteterrarenderer.draco.attributes.PointAttribute;
import com.mndk.bteterrarenderer.draco.attributes.PointIndex;
import com.mndk.bteterrarenderer.draco.compression.config.AttributeEncoderType;
import com.mndk.bteterrarenderer.draco.compression.pointcloud.PointCloudEncoder;
import com.mndk.bteterrarenderer.draco.core.EncoderBuffer;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.draco.core.StatusChain;
import com.mndk.bteterrarenderer.datatype.vector.CppVector;
import com.mndk.bteterrarenderer.draco.pointcloud.PointCloud;

import java.util.ArrayList;
import java.util.List;

public class SequentialAttributeEncodersController extends AttributesEncoder {

    private final List<SequentialAttributeEncoder> sequentialEncoders = new ArrayList<>();
    private final CppVector<Boolean> sequentialEncoderMarkedAsParent = new CppVector<>(DataType.bool());
    private final CppVector<PointIndex> pointIds = new CppVector<>(PointIndex.type());
    private final PointsSequencer sequencer;

    public SequentialAttributeEncodersController(PointsSequencer sequencer) {
        this.sequencer = sequencer;
    }

    public SequentialAttributeEncodersController(PointsSequencer sequencer, int pointAttributeId) {
        super(pointAttributeId);
        this.sequencer = sequencer;
    }

    @Override
    public Status init(PointCloudEncoder encoder, PointCloud pc) {
        StatusChain chain = new StatusChain();

        if (super.init(encoder, pc).isError(chain)) return chain.get();
        if (this.createSequentialEncoders().isError(chain)) return chain.get();

        // Initialize all value encoders.
        for (int i = 0; i < this.getNumAttributes(); ++i) {
            int attId = getAttributeId(i);
            if (sequentialEncoders.get(i).init(encoder, attId).isError(chain)) return chain.get();
        }
        return Status.ok();
    }

    @Override
    public Status encodeAttributesEncoderData(EncoderBuffer outBuffer) {
        StatusChain chain = new StatusChain();

        if (super.encodeAttributesEncoderData(outBuffer).isError(chain)) return chain.get();

        // Encode a unique id of every sequential encoder.
        for (SequentialAttributeEncoder sequentialEncoder : sequentialEncoders) {
            outBuffer.encode(sequentialEncoder.getUniqueId());
        }
        return Status.ok();
    }

    @Override
    public Status encodeAttributes(EncoderBuffer buffer) {
        StatusChain chain = new StatusChain();

        if (sequencer == null) return Status.dracoError("Sequencer is null");
        if (sequencer.generateSequence(pointIds).isError(chain)) return chain.get();
        return super.encodeAttributes(buffer);
    }

    @Override
    public UByte getUniqueId() {
        return UByte.of(AttributeEncoderType.BASIC_ATTRIBUTE_ENCODER.getValue());
    }

    @Override
    public int getNumParentAttributes(int pointAttributeId) {
        int locId = getLocalIdForPointAttribute(pointAttributeId);
        if (locId < 0) return 0;
        return sequentialEncoders.get(locId).getNumParentAttributes();
    }

    @Override
    public int getParentAttributeId(int pointAttributeId, int parentIndex) {
        int locId = getLocalIdForPointAttribute(pointAttributeId);
        if (locId < 0) return -1;
        return sequentialEncoders.get(locId).getParentAttributeId(parentIndex);
    }

    @Override
    public Status markParentAttribute(int pointAttributeId) {
        int locId = getLocalIdForPointAttribute(pointAttributeId);
        if (locId < 0) return Status.ok();
        // Mark the attribute encoder as parent (even when if it is not created yet).
        if (sequentialEncoderMarkedAsParent.size() <= locId) {
            sequentialEncoderMarkedAsParent.resize(locId + 1, false);
        }
        sequentialEncoderMarkedAsParent.set(locId, true);

        if (sequentialEncoders.size() > locId) {
            // Sequential encoders are generated.
            sequentialEncoders.get(locId).markParentAttribute();
        }
        return Status.ok();
    }

    @Override
    public PointAttribute getPortableAttribute(int pointAttributeId) {
        int locId = getLocalIdForPointAttribute(pointAttributeId);
        if (locId < 0) return null;
        return sequentialEncoders.get(locId).getPortableAttribute();
    }

    @Override
    protected Status transformAttributesToPortableFormat() {
        StatusChain chain = new StatusChain();

        for (SequentialAttributeEncoder sequentialEncoder : sequentialEncoders) {
            if (sequentialEncoder.transformAttributeToPortableFormat(pointIds).isError(chain)) return chain.get();
        }
        return Status.ok();
    }

    @Override
    protected Status encodePortableAttributes(EncoderBuffer outBuffer) {
        StatusChain chain = new StatusChain();

        for (SequentialAttributeEncoder sequentialEncoder : sequentialEncoders) {
            if (sequentialEncoder.encodePortableAttribute(pointIds, outBuffer).isError(chain)) return chain.get();
        }
        return Status.ok();
    }

    @Override
    protected Status encodeDataNeededByPortableTransforms(EncoderBuffer outBuffer) {
        StatusChain chain = new StatusChain();

        for (SequentialAttributeEncoder sequentialEncoder : sequentialEncoders) {
            if (sequentialEncoder.encodeDataNeededByPortableTransform(outBuffer).isError(chain)) return chain.get();
        }
        return Status.ok();
    }

    protected Status createSequentialEncoders() {
        for (int i = 0; i < this.getNumAttributes(); ++i) {
            SequentialAttributeEncoder sequentialEncoder = this.createSequentialEncoder(i);
            if (sequentialEncoder == null) return Status.dracoError("Failed to create sequential encoder");
            sequentialEncoders.add(sequentialEncoder);
            if (i < sequentialEncoderMarkedAsParent.size()) {
                if (sequentialEncoderMarkedAsParent.get(i)) {
                    sequentialEncoder.markParentAttribute();
                }
            }
        }
        return Status.ok();
    }

    protected SequentialAttributeEncoder createSequentialEncoder(int i) {
        int attId = this.getAttributeId(i);
        PointCloudEncoder encoder = this.getPointCloudEncoder();
        PointAttribute att = encoder.getPointCloud().getAttribute(attId);

        switch (att.getDataType()) {
            case UINT8:
            case INT8:
            case UINT16:
            case INT16:
            case UINT32:
            case INT32:
                return new SequentialIntegerAttributeEncoder();
            case FLOAT32:
                if (encoder.getOptions().getAttributeInt(attId, "quantization_bits", -1) > 0) {
                    if (att.getAttributeType() == GeometryAttribute.Type.NORMAL) {
                        return new SequentialNormalAttributeEncoder();
                    } else {
                        return new SequentialQuantizationAttributeEncoder();
                    }
                }
                break;
            default:
                break;
        }
        return new SequentialAttributeEncoder();
    }
}
