package com.mndk.bteterrarenderer.ogc3dtiles.geoid;

import com.mndk.bteterrarenderer.ogc3dtiles.math.Spheroid3;

public interface GeoidHeightFunction {
    GeoidHeightFunction WGS84_ELLIPSOID = spheroid3 -> 0;
    GeoidHeightFunction EGM96_WW15MGH = new Egm96Ww15mghData();

    /**
     * Get the height of the geoid at the given point
     * @param spheroid3 The point to get the height of the geoid at.
     *                  For simplicity, the height property value is ignored.
     * @return The height of the geoid at the given point
     */
    double getHeight(Spheroid3 spheroid3);
}
