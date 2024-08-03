package com.mndk.bteterrarenderer.draco.compression.attributes;

import com.mndk.bteterrarenderer.draco.attributes.PointAttribute;
import com.mndk.bteterrarenderer.draco.attributes.PointIndex;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.datatype.vector.CppVector;
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
