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

package com.mndk.bteterrarenderer.draco.compression.attributes.scheme;

import com.mndk.bteterrarenderer.draco.attributes.PointAttribute;
import com.mndk.bteterrarenderer.draco.compression.config.PredictionSchemeMethod;
import com.mndk.bteterrarenderer.draco.compression.config.PredictionSchemeTransformType;
import com.mndk.bteterrarenderer.draco.core.Status;

public interface PSchemeInterface {

    PredictionSchemeMethod getPredictionMethod();

    /** Returns the encoded attribute. */
    PointAttribute getAttribute();

    /** Returns true when the prediction scheme is initialized with all data it needs. */
    boolean isInitialized();

    /** Returns the number of parent attributes that are needed for the prediction. */
    int getNumParentAttributes();

    /** Returns the type of each of the parent attribute. */
    PointAttribute.Type getParentAttributeType(int i);

    /**
     * Sets the required parent attribute.
     * Returns error if the attribute doesn't meet the requirements of the
     * prediction scheme.
     */
    Status setParentAttribute(PointAttribute att);

    /**
     * Method should return true if the prediction scheme guarantees that all
     * correction values are always positive (or at least non-negative).
     */
    boolean areCorrectionsPositive();

    /** Returns the transform type used by the prediction scheme. */
    PredictionSchemeTransformType getTransformType();

}
