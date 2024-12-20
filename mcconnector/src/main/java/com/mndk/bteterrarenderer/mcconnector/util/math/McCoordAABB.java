package com.mndk.bteterrarenderer.mcconnector.util.math;

public class McCoordAABB {
    public final McCoord min, max;

    public McCoordAABB(McCoord point) {
        this(point, point);
    }
    public McCoordAABB(McCoord min, McCoord max) {
        this.min = min;
        this.max = max;
    }

    public McCoordAABB include(McCoord point) {
        McCoord min = new McCoord(
                Math.min(this.min.getX(), point.getX()),
                Math.min(this.min.getY(), point.getY()),
                Math.min(this.min.getZ(), point.getZ())
        );
        McCoord max = new McCoord(
                Math.max(this.max.getX(), point.getX()),
                Math.max(this.max.getY(), point.getY()),
                Math.max(this.max.getZ(), point.getZ())
        );
        return new McCoordAABB(min, max);
    }

    public McCoordAABB include(McCoordAABB box) {
        McCoord min = new McCoord(
                Math.min(this.min.getX(), box.min.getX()),
                Math.min(this.min.getY(), box.min.getY()),
                Math.min(this.min.getZ(), box.min.getZ())
        );
        McCoord max = new McCoord(
                Math.max(this.max.getX(), box.max.getX()),
                Math.max(this.max.getY(), box.max.getY()),
                Math.max(this.max.getZ(), box.max.getZ())
        );
        return new McCoordAABB(min, max);
    }

    public McCoordAABB transform(McCoordTransformer transformer) {
        return new McCoordAABB(transformer.transform(this.min), transformer.transform(this.max));
    }
}
