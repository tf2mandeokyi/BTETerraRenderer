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

import com.mndk.bteterrarenderer.draco.attributes.PointAttribute;
import com.mndk.bteterrarenderer.draco.compression.pointcloud.PointCloudDecoder;
import com.mndk.bteterrarenderer.draco.core.DecoderBuffer;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.draco.pointcloud.PointCloud;

/**
 * Interface class for decoding one or more attributes that were encoded with a
 * matching AttributesEncoder. It provides only the basic interface
 * that is used by the PointCloudDecoder. The actual decoding must be
 * implemented in derived classes using the DecodeAttributes() method.
 */
public interface AttributesDecoderInterface {

    /**
     * Called after all attribute decoders are created. It can be used to perform
     * any custom initialization.
     */
    Status init(PointCloudDecoder decoder, PointCloud pc);

    /**
     * Decodes any attribute decoder specific data from the |in_buffer|.
     */
    Status decodeAttributesDecoderData(DecoderBuffer inBuffer);

    /**
     * Decode attribute data from the source buffer. Needs to be implemented by
     * the derived classes.
     */
    Status decodeAttributes(DecoderBuffer inBuffer);

    int getAttributeId(int i);
    int getNumAttributes();
    PointCloudDecoder getDecoder();

    /**
     * Returns an attribute containing data processed by the attribute transform.
     * (see TransformToPortableFormat() method). This data is guaranteed to be
     * same for encoder and decoder, and it can be used by predictors.
     */
    default PointAttribute getPortableAttribute(int pointAttributeId) {
        return null;
    }
}
