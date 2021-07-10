package com.mndk.bteterrarenderer.projection;

import net.buildtheearth.terraplusplus.generator.EarthGeneratorSettings;
import net.buildtheearth.terraplusplus.projection.GeographicProjection;

public class Projections {
	
	private static final GeographicProjection BTE;
	private static GeographicProjection SERVER_PROJECTION;
	
	public static final GeographicProjection KAKAO_WTM = new Proj4jProjection("EPSG:5186", new String[] {
			"+proj=tmerc", "+lat_0=38", "+lon_0=127", "+k=1", "+x_0=200000", "+y_0=500000", "+ellps=GRS80", "+units=m", "+no_defs"
	});

	public static void setDefaultBTEProjection() {
		SERVER_PROJECTION = BTE;
	}

	public static void setServerProjection(GeographicProjection proj) {
		SERVER_PROJECTION = proj;
	}

	public static GeographicProjection getServerProjection() {
		return SERVER_PROJECTION;
	}

	static {
		BTE = EarthGeneratorSettings.parse(EarthGeneratorSettings.BTE_DEFAULT_SETTINGS).projection();
	}
}
