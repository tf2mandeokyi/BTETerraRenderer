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

import com.mndk.bteterrarenderer.datatype.vector.CppVector;
import com.mndk.bteterrarenderer.draco.attributes.PointAttribute;
import com.mndk.bteterrarenderer.draco.attributes.PointIndex;
import com.mndk.bteterrarenderer.draco.core.Status;
import lombok.Getter;

@Getter
public abstract class PointsSequencer {

    private CppVector<PointIndex> outPointIds = null;

    public Status generateSequence(CppVector<PointIndex> outPointIds) {
        this.outPointIds = outPointIds;
        return this.generateSequenceInternal();
    }

    public void addPointId(PointIndex pointId) {
        outPointIds.pushBack(pointId);
    }

    public Status updatePointToAttributeIndexMapping(PointAttribute attribute) {
        return Status.unsupportedFeature("This sequencer does not support updating point to attribute index mapping.");
    }

    protected abstract Status generateSequenceInternal();

}
