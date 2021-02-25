package com.mndk.mapdisp4bte.projection;

import net.buildtheearth.terraplusplus.projection.GeographicProjection;
import net.buildtheearth.terraplusplus.projection.dymaxion.BTEDymaxionProjection;

public class Projections {
    public static final GeographicProjection BTE = new BTEDymaxionProjection();
    public static final GeographicProjection WTM = new WTMProjection();
}
