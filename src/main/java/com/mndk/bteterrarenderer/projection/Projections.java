package com.mndk.bteterrarenderer.projection;

import net.buildtheearth.terraplusplus.generator.EarthGeneratorSettings;
import net.buildtheearth.terraplusplus.projection.GeographicProjection;

public class Projections {
	
	public static final GeographicProjection BTE;
	
	public static final GeographicProjection KAKAO_WTM = new Proj4jProjection("EPSG:5186", new String[] {
			"+proj=tmerc", "+lat_0=38", "+lon_0=127", "+k=1", "+x_0=200000", "+y_0=500000", "+ellps=GRS80", "+units=m", "+no_defs"
	});

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
