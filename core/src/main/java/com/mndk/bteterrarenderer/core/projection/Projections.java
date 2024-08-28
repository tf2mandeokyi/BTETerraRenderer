package com.mndk.bteterrarenderer.core.projection;

import com.mndk.bteterrarenderer.core.config.BTETerraRendererConfig;
import com.mndk.bteterrarenderer.dep.terraplusplus.projection.GeographicProjection;
import net.daporkchop.lib.binary.oio.StreamUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public class Projections {

	public static final GeographicProjection BTE;
	private static GeographicProjection SERVER_PROJECTION;

	public static void updateHologramProjection() {
		SERVER_PROJECTION = Optional.ofNullable(BTETerraRendererConfig.HOLOGRAM.projectionJson)
				.map(GeographicProjection::parse)
				.orElse(BTE);
	}

	public static void setHologramProjection(GeographicProjection proj) {
		SERVER_PROJECTION = Optional.ofNullable(proj).orElse(BTE);
	}
	public static GeographicProjection getHologramProjection() {
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
