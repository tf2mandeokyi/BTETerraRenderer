package com.mndk.bteterrarenderer.projection;

import net.buildtheearth.terraplusplus.generator.EarthGeneratorSettings;
import net.buildtheearth.terraplusplus.projection.GeographicProjection;

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
		BTE = EarthGeneratorSettings.parse(BTE_GEN_JSON).projection();
	}
}
