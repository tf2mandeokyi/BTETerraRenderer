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

package com.mndk.bteterrarenderer.draco.compression.pointcloud;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.UByte;
import com.mndk.bteterrarenderer.datatype.number.UShort;
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.datatype.vector.CppVector;
import com.mndk.bteterrarenderer.draco.attributes.PointAttribute;
import com.mndk.bteterrarenderer.draco.compression.attributes.AttributesEncoder;
import com.mndk.bteterrarenderer.draco.compression.config.DracoHeader;
import com.mndk.bteterrarenderer.draco.compression.config.DracoVersions;
import com.mndk.bteterrarenderer.draco.compression.config.EncodedGeometryType;
import com.mndk.bteterrarenderer.draco.compression.config.EncoderOptions;
import com.mndk.bteterrarenderer.draco.core.EncoderBuffer;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.draco.core.StatusChain;
import com.mndk.bteterrarenderer.draco.metadata.MetadataEncoder;
import com.mndk.bteterrarenderer.draco.pointcloud.PointCloud;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public abstract class PointCloudEncoder {

    @Getter
    @Setter
    private PointCloud pointCloud;
    private final List<AttributesEncoder> attributesEncoders = new ArrayList<>();
    private final CppVector<Integer> attributeToEncoderMap = new CppVector<>(DataType.int32());
    private final CppVector<Integer> attributesEncoderIdsOrder = new CppVector<>(DataType.int32());
    @Getter
    private EncoderBuffer buffer;
    @Getter
    private EncoderOptions options;
    @Getter
    @Setter(AccessLevel.PROTECTED)
    private int numEncodedPoints;

    public Status encode(EncoderOptions options, EncoderBuffer outBuffer) {
        StatusChain chain = new StatusChain();

        this.options = options;
        this.buffer = outBuffer;

        attributesEncoders.clear();
        attributeToEncoderMap.clear();
        attributesEncoderIdsOrder.clear();

        if(pointCloud == null) {
            return Status.dracoError("Invalid input geometry.");
        }
        if(this.encodeHeader().isError(chain)) return chain.get();
        if(this.encodeMetadata().isError(chain)) return chain.get();
        if(this.initializeEncoder().isError(chain)) return chain.get();
        if(this.encodeEncoderData().isError(chain)) return chain.get();
        if(this.encodeGeometryData().isError(chain)) return chain.get();
        if(this.encodePointAttributes().isError(chain)) return chain.get();
        if(options.getGlobalBool("store_number_of_encoded_points", false)) {
            this.computeNumberOfEncodedPoints();
        }
        return Status.ok();
    }

    public EncodedGeometryType getGeometryType() {
        return EncodedGeometryType.POINT_CLOUD;
    }

    public abstract UByte getEncodingMethod();

    public int getNumAttributesEncoders() {
        return attributesEncoders.size();
    }

    public AttributesEncoder getAttributesEncoder(int i) {
        return attributesEncoders.get(i);
    }

    public int addAttributesEncoder(AttributesEncoder attEnc) {
        attributesEncoders.add(attEnc);
        return attributesEncoders.size() - 1;
    }

    public Status markParentAttribute(int parentAttId) {
        if(parentAttId < 0 || parentAttId >= pointCloud.getNumAttributes()) {
            return Status.invalidParameter("Invalid parent attribute ID");
        }
        int parentAttEncoderId = attributeToEncoderMap.get(parentAttId);
        AttributesEncoder parentAttEncoder = attributesEncoders.get(parentAttEncoderId);
        return parentAttEncoder.markParentAttribute(parentAttId);
    }

    public PointAttribute getPortableAttribute(int parentAttributeId) {
        if(parentAttributeId < 0 || parentAttributeId >= pointCloud.getNumAttributes()) {
            return null;
        }
        int parentAttEncoderId = attributeToEncoderMap.get(parentAttributeId);
        AttributesEncoder parentAttEncoder = attributesEncoders.get(parentAttEncoderId);
        return parentAttEncoder.getPortableAttribute(parentAttributeId);
    }

    protected Status initializeEncoder() { return Status.ok(); }

    protected Status encodeEncoderData() { return Status.ok(); }

    protected Status encodeGeometryData() { return Status.ok(); }

    protected Status encodePointAttributes() {
        StatusChain chain = new StatusChain();

        if(this.generateAttributesEncoders().isError(chain)) return chain.get();

        // Encode the number of attribute encoders.
        buffer.encode(UByte.of(attributesEncoders.size()));

        // Initialize all the encoders (this is used for example to init attribute
        // dependencies, no data is encoded in this step).
        for(AttributesEncoder attEnc : attributesEncoders) {
            if(attEnc.init(this, pointCloud).isError(chain)) return chain.get();
        }

        // Rearrange attributes to respect dependencies between individual attributes.
        if(this.rearrangeAttributesEncoders().isError(chain)) return chain.get();

        // Encode any data that is necessary to create the corresponding attribute
        // decoder.
        for(int attEncoderId : attributesEncoderIdsOrder) {
            if(this.encodeAttributesEncoderIdentifier(attEncoderId).isError(chain)) return chain.get();
        }

        // Also encode any attribute encoder data (such as the info about encoded
        // attributes).
        for(int attEncoderId : attributesEncoderIdsOrder) {
            AttributesEncoder attEncoder = attributesEncoders.get(attEncoderId);
            if(attEncoder.encodeAttributesEncoderData(buffer).isError(chain)) return chain.get();
        }

        // Lastly encode all the attributes using the provided attribute encoders.
        return this.encodeAllAttributes();
    }

    protected Status generateAttributesEncoders() {
        StatusChain chain = new StatusChain();

        for(int i = 0; i < pointCloud.getNumAttributes(); ++i) {
            if(this.generateAttributesEncoder(i).isError(chain)) return chain.get();
        }
        attributeToEncoderMap.resize(pointCloud.getNumAttributes());
        for(int i = 0; i < attributesEncoders.size(); ++i) {
            AttributesEncoder attEnc = attributesEncoders.get(i);
            for(int j = 0; j < attEnc.getNumAttributes(); ++j) {
                attributeToEncoderMap.set(attEnc.getAttributeId(j), i);
            }
        }
        return Status.ok();
    }

    protected abstract Status generateAttributesEncoder(int attId);

    protected Status encodeAttributesEncoderIdentifier(int attEncoderId) {
        return Status.ok();
    }

    protected Status encodeAllAttributes() {
        StatusChain chain = new StatusChain();

        for(int attEncoderId : attributesEncoderIdsOrder) {
            AttributesEncoder attEncoder = attributesEncoders.get(attEncoderId);
            if(attEncoder.encodeAttributes(buffer).isError(chain)) return chain.get();
        }
        return Status.ok();
    }

    protected abstract void computeNumberOfEncodedPoints();

    private Status encodeHeader() {
        buffer.encode(Pointer.wrap("DRACO".getBytes()), 5);
        EncodedGeometryType encoderType = this.getGeometryType();
        // Version (major, minor).
        byte versionMajor = encoderType == EncodedGeometryType.POINT_CLOUD
                ? DracoVersions.POINT_CLOUD_BIT_STREAM_VERSION_MAJOR
                : DracoVersions.MESH_BIT_STREAM_VERSION_MAJOR;
        byte versionMinor = encoderType == EncodedGeometryType.POINT_CLOUD
                ? DracoVersions.POINT_CLOUD_BIT_STREAM_VERSION_MINOR
                : DracoVersions.MESH_BIT_STREAM_VERSION_MINOR;

        buffer.encode(UByte.of(versionMajor));
        buffer.encode(UByte.of(versionMinor));
        // Type of the encoder (point cloud, mesh, ...).
        buffer.encode(UByte.of(encoderType.getValue()));
        // Unique identifier for the selected encoding method (edgebreaker, etc...).
        buffer.encode(this.getEncodingMethod());
        // Reserved for flags.
        UShort flags = UShort.of(0);
        // First bit of {@code flags} is reserved for metadata.
        if(pointCloud.getMetadata() != null) {
            flags = flags.or(DracoHeader.METADATA_FLAG_MASK);
        }
        buffer.encode(flags);
        return Status.ok();
    }

    private Status encodeMetadata() {
        StatusChain chain = new StatusChain();
        if(pointCloud.getMetadata() == null) {
            return Status.ok();
        }
        MetadataEncoder metadataEncoder = new MetadataEncoder();
        if(metadataEncoder.encodeGeometryMetadata(buffer, pointCloud.getMetadata()).isError(chain)) return chain.get();
        return Status.ok();
    }

    @SuppressWarnings("ConstantValue")
    private Status rearrangeAttributesEncoders() {
        // Find the encoding order of the attribute encoders that is determined by
        // the parent dependencies between individual encoders.
        attributesEncoderIdsOrder.resize(attributesEncoders.size());
        CppVector<Boolean> isEncoderProcessed = new CppVector<>(DataType.bool(), attributesEncoders.size(), false);
        int numProcessedEncoders = 0;
        while(numProcessedEncoders < attributesEncoders.size()) {
            // Flagged when any of the encoder get processed.
            boolean encoderProcessed = false;
            for(int i = 0; i < attributesEncoders.size(); ++i) {
                if(isEncoderProcessed.get(i)) {
                    continue;  // Encoder already processed.
                }
                // Check if all parent encoders are already processed.
                boolean canBeProcessed = true;
                AttributesEncoder attEnc = attributesEncoders.get(i);
                for(int p = 0; p < attEnc.getNumAttributes(); ++p) {
                    int attId = attEnc.getAttributeId(p);
                    for(int ap = 0; ap < attEnc.getNumParentAttributes(attId); ++ap) {
                        int parentAttId = attEnc.getParentAttributeId(attId, ap);
                        int parentEncoderId = attributeToEncoderMap.get(parentAttId);
                        if(parentAttId != i && !isEncoderProcessed.get(parentEncoderId)) {
                            canBeProcessed = false;
                            break;
                        }
                    }
                }
                if(!canBeProcessed) {
                    continue;  // Try to process the encoder in the next iteration.
                }
                // Encoder can be processed. Update the encoding order.
                attributesEncoderIdsOrder.set(numProcessedEncoders++, i);
                isEncoderProcessed.set(i, true);
                encoderProcessed = true;
            }
            if(!encoderProcessed && numProcessedEncoders < attributesEncoders.size()) {
                // No encoder was processed but there are still some remaining unprocessed
                // encoders.
                return Status.dracoError("Failed to rearrange attributes encoders");
            }
        }

        // Now for every encoder, reorder the attributes to satisfy their
        // dependencies.
        CppVector<Integer> attributeEncodingOrder = new CppVector<>(DataType.int32());
        CppVector<Boolean> isAttributeProcessed = new CppVector<>(DataType.bool(), pointCloud.getNumAttributes(), false);

        for(int aeOrder = 0; aeOrder < attributesEncoders.size(); aeOrder++) {
            int ae = attributesEncoderIdsOrder.get(aeOrder);
            int numEncoderAttributes = attributesEncoders.get(ae).getNumAttributes();
            if(numEncoderAttributes < 2) {
                continue;  // No need to resolve dependencies for a single attribute.
            }

            int numProcessedAttributes = 0;
            attributeEncodingOrder.resize(numEncoderAttributes);
            while(numProcessedAttributes < numEncoderAttributes) {
                // Flagged when any of the attributes get processed.
                boolean attributeProcessed = false;
                for(int i = 0; i < numEncoderAttributes; ++i) {
                    int attId = attributesEncoders.get(ae).getAttributeId(i);
                    if(isAttributeProcessed.get(i)) {
                        continue;  // Attribute already processed.
                    }
                    // Check if all parent attributes are already processed.
                    boolean canBeProcessed = true;
                    for(int p = 0; p < attributesEncoders.get(ae).getNumParentAttributes(attId); ++p) {
                        int parentAttId = attributesEncoders.get(ae).getParentAttributeId(attId, p);
                        if(!isAttributeProcessed.get(parentAttId)) {
                            canBeProcessed = false;
                            break;
                        }
                    }
                    if(!canBeProcessed) {
                        continue;  // Try to process the attribute in the next iteration.
                    }
                    // Attribute can be processed. Update the encoding order.
                    attributeEncodingOrder.set(numProcessedAttributes++, i);
                    isAttributeProcessed.set(i, true);
                    attributeProcessed = true;
                }
                if(!attributeProcessed && numProcessedAttributes < numEncoderAttributes) {
                    // No attribute was processed but there are still some remaining
                    // unprocessed attributes.
                    return Status.dracoError("Failed to rearrange attributes encoders");
                }
            }
            // Update the order of the attributes within the encoder.
            attributesEncoders.get(ae).setAttributeIds(attributeEncodingOrder);
        }
        return Status.ok();
    }

}
