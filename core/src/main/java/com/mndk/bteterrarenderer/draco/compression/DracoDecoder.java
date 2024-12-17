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

package com.mndk.bteterrarenderer.draco.compression;

import com.mndk.bteterrarenderer.draco.attributes.GeometryAttribute;
import com.mndk.bteterrarenderer.draco.compression.config.*;
import com.mndk.bteterrarenderer.draco.compression.mesh.MeshDecoder;
import com.mndk.bteterrarenderer.draco.compression.mesh.MeshEdgebreakerDecoder;
import com.mndk.bteterrarenderer.draco.compression.mesh.MeshSequentialDecoder;
import com.mndk.bteterrarenderer.draco.compression.pointcloud.PointCloudDecoder;
import com.mndk.bteterrarenderer.draco.core.*;
import com.mndk.bteterrarenderer.draco.mesh.Mesh;
import com.mndk.bteterrarenderer.draco.pointcloud.PointCloud;
import io.netty.buffer.ByteBuf;
import lombok.Getter;

import java.io.IOException;
import java.io.InputStream;

/**
 * Class responsible for decoding of meshes and point clouds that were
 * compressed by a Draco encoder.
 */
@Getter
public class DracoDecoder {

    private final DecoderOptions options = new DecoderOptions();

    /**
     * Returns the geometry type encoded in the input {@code inBuffer}.
     * The return value is one of {@link EncodedGeometryType#POINT_CLOUD},
     * {@link EncodedGeometryType#TRIANGULAR_MESH} or {@link EncodedGeometryType#INVALID_GEOMETRY_TYPE}
     * in case the input data is invalid.
     * The decoded geometry type can be used to choose an appropriate decoding
     * function for a given geometry type. (see below)
     */
    public static StatusOr<EncodedGeometryType> getEncodedGeometryType(DecoderBuffer inBuffer) {
        StatusChain chain = new StatusChain();
        DecoderBuffer tempBuffer = new DecoderBuffer(inBuffer);
        DracoHeader header = new DracoHeader();
        if (PointCloudDecoder.decodeHeader(tempBuffer, header).isError(chain)) return StatusOr.error(chain.get());
        return StatusOr.ok(header.getEncoderType());
    }

    /**
     * Decodes point cloud from the provided buffer. The buffer must be filled
     * with data that was encoded with either the {@link DracoEncoder#encodePointCloudToBuffer} or
     * {@link DracoEncoder#encodeMeshToBuffer} methods. In case the input buffer contains
     * mesh, the returned instance can be down-casted to {@link Mesh}.
     */
    public StatusOr<PointCloud> decodePointCloudFromBuffer(DecoderBuffer inBuffer) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    /**
     * Decodes a triangular mesh from the provided buffer. The mesh must be filled
     * with data that was encoded using the {@link DracoEncoder#encodeMeshToBuffer} method.
     * The function will return {@code null} in case the input is invalid or if it was
     * encoded with the {@link DracoEncoder#encodePointCloudToBuffer} method.
     */
    public StatusOr<Mesh> decodeMeshFromBuffer(DecoderBuffer inBuffer) {
        StatusChain chain = new StatusChain();
        Mesh mesh = new Mesh();
        if (this.decodeBufferToGeometry(inBuffer, mesh).isError(chain)) return StatusOr.error(chain.get());
        return StatusOr.ok(mesh);
    }

    public StatusOr<Mesh> decodeMeshFromBuffer(ByteBuf buf) {
        DecoderBuffer decoderBuffer = new DecoderBuffer();
        decoderBuffer.init(buf);
        return this.decodeMeshFromBuffer(decoderBuffer);
    }

    public StatusOr<Mesh> decodeMeshFromStream(InputStream stream) throws IOException {
        DecoderBuffer decoderBuffer = new DecoderBuffer();
        decoderBuffer.init(stream);
        return this.decodeMeshFromBuffer(decoderBuffer);
    }

    /**
     * Decodes the buffer into a provided geometry. If the geometry is
     * incompatible with the encoded data. For example, when {@code outGeometry} is
     * {@link Mesh} while the data contains a point cloud, the function will return
     * an error status.
     */
    public Status decodeBufferToGeometry(DecoderBuffer inBuffer, PointCloud outGeometry) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    private static StatusOr<PointCloudDecoder> createPointCloudDecoder(PointCloudEncodingMethod method) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    /**
     * Decodes the buffer into a provided geometry. If the geometry is
     * incompatible with the encoded data. For example, when {@code outGeometry} is
     * {@link PointCloud} while the data contains a mesh, the function will return
     * an error status.
     */
    public Status decodeBufferToGeometry(DecoderBuffer inBuffer, Mesh outGeometry) {
        StatusChain chain = new StatusChain();
        DecoderBuffer tempBuffer = new DecoderBuffer(inBuffer);
        // Parse header
        DracoHeader header = new DracoHeader();
        if (PointCloudDecoder.decodeHeader(tempBuffer, header).isError(chain)) return chain.get();
        if (header.getEncoderType() != EncodedGeometryType.TRIANGULAR_MESH) {
            return Status.dracoError("Input is not a mesh.");
        }
        // Get decoder
        StatusOr<MeshDecoder> decoderOrError = createMeshDecoder(header.getEncoderMethod());
        if (decoderOrError.isError(chain)) return chain.get();
        MeshDecoder decoder = decoderOrError.getValue();
        // Decode
        return decoder.decode(options, inBuffer, outGeometry);
    }

    private static StatusOr<MeshDecoder> createMeshDecoder(MeshEncoderMethod method) {
        if (method == MeshEncoderMethod.SEQUENTIAL) {
            return StatusOr.ok(new MeshSequentialDecoder());
        } else if (method == MeshEncoderMethod.EDGEBREAKER) {
            return StatusOr.ok(new MeshEdgebreakerDecoder());
        }
        return StatusOr.dracoError("Unsupported encoding method.");
    }

    /**
     * When set, the decoder is going to skip attribute transform for a given
     * attribute type. For example for quantized attributes, the decoder would
     * skip the dequantization step and the returned geometry would contain an
     * attribute with quantized values. The attribute would also contain an
     * instance of AttributeTransform class that is used to describe the skipped
     * transform, including all parameters that are needed to perform the
     * transform manually.
     */
    public void setSkipAttributeTransform(GeometryAttribute.Type attType) {
        options.setAttributeBool(attType, "skip_attribute_transform", true);
    }

}
