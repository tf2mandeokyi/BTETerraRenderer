package com.mndk.bteterrarenderer.projection;

import com.mndk.bteterrarenderer.dep.terraplusplus.projection.GeographicProjection;
import net.daporkchop.lib.binary.oio.StreamUtil;

import java.io.IOException;
import java.io.InputStream;

public class Projections {

	public static final GeographicProjection BTE;
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
		try(InputStream in = Projections.class.getResourceAsStream("bte_projection_settings.json5")) {
			if(in == null) throw new IOException("Resource bte_projection_settings.json5 not found");
			BTE = GeographicProjection.parse(new String(StreamUtil.toByteArray(in)));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
