package com.mndk.bteterrarenderer.ogc3dtiles.math;

import com.mndk.bteterrarenderer.ogc3dtiles.Wgs84Constants;
import lombok.experimental.UtilityClass;

@UtilityClass
public class EllipsoidalMath {

    /**
     * @link <a href="https://gssc.esa.int/navipedia/index.php/Ellipsoidal_and_Cartesian_Coordinates_Conversion">
     *     Ellipsoidal and Cartesian Coordinates Conversion</a>
     * @param latitude The latitude, in radians
     * @return The radius of curvature in the prime vertical
     */
    public double getEarthCurvatureRadius(double latitude) {
        return Wgs84Constants.SEMI_MAJOR_AXIS /
                Math.sqrt(1 - (Wgs84Constants.ECCENTRICITY2 * Math.pow(Math.sin(latitude), 2)));
    }

}
