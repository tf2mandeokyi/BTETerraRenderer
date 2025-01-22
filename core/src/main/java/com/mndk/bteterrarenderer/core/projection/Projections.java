package com.mndk.bteterrarenderer.core.projection;

import com.mndk.bteterrarenderer.core.config.BTETerraRendererConfig;
import com.mndk.bteterrarenderer.dep.terraplusplus.projection.GeographicProjection;
import net.daporkchop.lib.binary.oio.StreamUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;

public class Projections {

	public static final GeographicProjection BTE;
	@Nullable
	private static GeographicProjection SERVER_PROJECTION;

	public static void updateHologramProjection() {
		String json = BTETerraRendererConfig.HOLOGRAM.projectionJson;
        if (json == null || json.isEmpty()) return;
        GeographicProjection parse = GeographicProjection.parse(json);
        if (parse != null) SERVER_PROJECTION = parse;
    }

	public static void setHologramProjection(@Nullable GeographicProjection proj) {
		SERVER_PROJECTION = proj;
	}

	@Nonnull
	public static GeographicProjection getHologramProjection() {
		return SERVER_PROJECTION != null ? SERVER_PROJECTION : BTE;
	}

	static {
		try (InputStream in = Projections.class.getResourceAsStream("bte_projection_settings.json5")) {
			if (in == null) throw new IOException("Resource bte_projection_settings.json5 not found");
			BTE = GeographicProjection.parse(new String(StreamUtil.toByteArray(in)));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
