package com.mndk.bteterrarenderer.draco.compression.attributes;

import com.mndk.bteterrarenderer.draco.attributes.PointAttribute;
import com.mndk.bteterrarenderer.draco.attributes.PointIndex;
import com.mndk.bteterrarenderer.draco.core.Status;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LinearSequencer extends PointsSequencer {

    private final int numPoints;

    @Override
    public Status updatePointToAttributeIndexMapping(PointAttribute attribute) {
        attribute.setIdentityMapping();
        return Status.ok();
    }

    @Override
    protected Status generateSequenceInternal() {
        if (numPoints < 0) {
            return Status.dracoError("Invalid number of points");
        }
        this.getOutPointIds().resize(numPoints);
        for (int i = 0; i < numPoints; ++i) {
            this.getOutPointIds().set(i, PointIndex.of(i));
        }
        return Status.ok();
    }
}
