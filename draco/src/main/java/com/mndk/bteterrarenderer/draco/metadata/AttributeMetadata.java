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

package com.mndk.bteterrarenderer.draco.metadata;

import com.mndk.bteterrarenderer.datatype.number.UInt;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Class for representing specifically metadata of attributes. It must have an
 * attribute id which should be identical to its counterpart attribute in
 * the point cloud it belongs to.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AttributeMetadata extends Metadata {

    private UInt attUniqueId;

    public AttributeMetadata() {
        super();
        this.attUniqueId = UInt.ZERO;
    }

    public AttributeMetadata(Metadata metadata) {
        super(metadata);
        this.attUniqueId = UInt.ZERO;
    }

    public AttributeMetadata(AttributeMetadata metadata) {
        super(metadata);
        this.attUniqueId = metadata.attUniqueId;
    }

}
