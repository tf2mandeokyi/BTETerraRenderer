package com.mndk.bteterrarenderer.projection;

import net.buildtheearth.terraplusplus.generator.EarthGeneratorSettings;
import net.buildtheearth.terraplusplus.projection.GeographicProjection;

public class Projections {
	
	private static final GeographicProjection BTE;
	private static GeographicProjection SERVER_PROJECTION;

	public static void setDefaultBTEProjection() {
		SERVER_PROJECTION = BTE;
	}

	public static void setServerProjection(GeographicProjection proj) {
		SERVER_PROJECTION = proj == null ? BTE : proj;
	}

	public static GeographicProjection getServerProjection() {
		return SERVER_PROJECTION;
	}

	static {
		BTE = EarthGeneratorSettings.parse(EarthGeneratorSettings.BTE_DEFAULT_SETTINGS).projection();
	}
}
