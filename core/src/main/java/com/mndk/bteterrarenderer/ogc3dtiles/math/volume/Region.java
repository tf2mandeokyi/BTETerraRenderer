package com.mndk.bteterrarenderer.ogc3dtiles.math.volume;

import com.mndk.bteterrarenderer.ogc3dtiles.math.SpheroidCoordinatesConverter;
import com.mndk.bteterrarenderer.ogc3dtiles.math.UnitCube;
import com.mndk.bteterrarenderer.ogc3dtiles.math.matrix.Matrix4f;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class Region extends Volume {
    // read these from array in order
    /** In radians */
    private final double westLon, southLat, eastLon, northLat;
    private final double minHeight, maxHeight;

    @Override
    public boolean intersectsSphere(Sphere sphere, Matrix4f thisTransform) {
		Region[] sphereRegions = sphere.toBoundingRegions();
		for(Region sphereRegion : sphereRegions) {
			if(this.intersectsRegion(sphereRegion)) return true;
		}
		return false;
    }

    @Override
    public boolean intersectsGeoCoordinateRay(double[] coordinateDegrees, Matrix4f thisTransform, SpheroidCoordinatesConverter converter) {
        double lonRad = Math.toRadians(coordinateDegrees[0]), latRad = Math.toRadians(coordinateDegrees[1]);
        if(westLon <= eastLon) {
            if(lonRad < westLon || eastLon < lonRad) return false;
        } else {
            if(eastLon < lonRad && westLon < lonRad) return false;
        }

        return southLat <= latRad && latRad <= northLat;
    }

	public boolean intersectsRegion(Region other) {
        if(!UnitCube.rangeIntersects(this.minHeight, this.maxHeight, other.minHeight, other.maxHeight))
            return false;
		if(!UnitCube.rangeIntersects(this.southLat, this.northLat, other.southLat, other.northLat))
			return false;

        if(this.westLon < this.eastLon) {
            if (other.westLon < other.eastLon) return
                    UnitCube.rangeIntersects(this.westLon, this.eastLon, other.westLon, other.eastLon);
            else return
                    UnitCube.rangeIntersects(this.westLon, this.eastLon, other.westLon,       Math.PI) ||
                    UnitCube.rangeIntersects(this.westLon, this.eastLon,      -Math.PI, other.eastLon);
        }
        else {
            if (other.westLon < other.eastLon) return
                    UnitCube.rangeIntersects(this.westLon,      Math.PI, other.westLon, other.eastLon) ||
                    UnitCube.rangeIntersects(    -Math.PI, this.eastLon, other.westLon, other.eastLon);
            else return
                    UnitCube.rangeIntersects(this.westLon,      Math.PI, other.westLon,       Math.PI) ||
                    UnitCube.rangeIntersects(this.westLon,      Math.PI,      -Math.PI, other.eastLon) ||
                    UnitCube.rangeIntersects(    -Math.PI, this.eastLon, other.westLon,       Math.PI) ||
                    UnitCube.rangeIntersects(    -Math.PI, this.eastLon,      -Math.PI, other.eastLon);
        }
	}

    public String toString() {
        return String.format(
                "Region(lon=[W=%.7f°, E=%.7f°], lat=[N=%.7f°, S=%.7f°], height=[%.1fm ~ %.1fm])",
                Math.toDegrees(westLon), Math.toDegrees(eastLon),
                Math.toDegrees(northLat), Math.toDegrees(southLat),
                minHeight, maxHeight
        );
    }

    public static Region fromArray(double[] array) {
        return new Region(array[0], array[1], array[2], array[3], array[4], array[5]);
    }
}
