/* This code is from https://hyosang82.tistory.com/269 */

package com.mndk.mapdisp4bte.projection;

import java.util.Arrays;

public class WTMProjection extends Proj4jProjection {
    public WTMProjection() {
        super("EPSG:5186", new String[] {
                "+proj=tmerc", "+lat_0=38", "+lon_0=127", "+k=1", "+x_0=200000", "+y_0=600000", "+ellps=GRS80", "+units=m", "+no_defs"
        });
    }

    public static void main(String[] args) {
        System.out.println(Arrays.toString(new WTMProjection().fromGeo(126.9950400894708, 37.4353519765839)));
        System.out.println(Arrays.toString(new WTM1Projection().fromGeo(126.9950400894708, 37.4353519765839)));
    }
}
