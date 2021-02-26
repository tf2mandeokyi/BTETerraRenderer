package com.mndk.bte_tr.projection;

import copy.io.github.terra121.generator.EarthGeneratorSettings;
import copy.io.github.terra121.projection.GeographicProjection;

public class Projections {
    public static final GeographicProjection BTE;
    public static final GeographicProjection WTM = new WTMProjection();

    static {
        final String BTE_GEN_JSON =
                "{" +
                    "\"projection\":\"bteairocean\"," +
                    "\"orentation\":\"upright\"," +
                    "\"scaleX\":7318261.522857145," +
                    "\"scaleY\":7318261.522857145" +
                "}";
        BTE = new EarthGeneratorSettings(BTE_GEN_JSON).getProjection();
    }
}
