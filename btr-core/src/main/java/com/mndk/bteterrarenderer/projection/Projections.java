package com.mndk.bteterrarenderer.projection;

import com.mndk.bteterrarenderer.connector.DependencyConnectorSupplier;
import com.mndk.bteterrarenderer.connector.terraplusplus.projection.IGeographicProjection;

public class Projections {
	
	private static final IGeographicProjection BTE;
	private static IGeographicProjection SERVER_PROJECTION;

	public static void setDefaultBTEProjection() {
		SERVER_PROJECTION = BTE;
	}

	public static void setServerProjection(IGeographicProjection proj) {
		SERVER_PROJECTION = proj == null ? BTE : proj;
	}

	public static IGeographicProjection getServerProjection() {
		return SERVER_PROJECTION;
	}

	static {
		BTE = DependencyConnectorSupplier.INSTANCE.getBTEProjection();
	}
}
