package copy.io.github.terra121.generator;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.mndk.bteterrarenderer.BTETerraRenderer;

import copy.io.github.terra121.projection.GeographicProjection;
import copy.io.github.terra121.projection.transform.OffsetProjectionTransform;
import copy.io.github.terra121.projection.transform.ScaleProjectionTransform;

public class EarthGeneratorSettings {

	private static final Gson GSON = new GsonBuilder().create();

	public JsonSettings settings;

	public EarthGeneratorSettings(String generatorSettings) {
		if (Strings.isNullOrEmpty(generatorSettings)) { //blank string means default
			this.settings = new JsonSettings();
		} else {
			try {
				this.settings = GSON.fromJson(generatorSettings, JsonSettings.class);
			} catch (JsonSyntaxException e) {
				BTETerraRenderer.logger.error("Invalid Earth Generator Settings, using default settings");
				this.settings = new JsonSettings();
			}
		}
	}

	@Override
	public String toString() {
		return GSON.toJson(this.settings, JsonSettings.class);
	}

	public GeographicProjection getProjection() {
		GeographicProjection projection  = GeographicProjection.projections.get(this.settings.projection);
		projection = GeographicProjection.orientProjection(projection, this.settings.orentation);
		projection = new ScaleProjectionTransform(projection, this.settings.scaleX, this.settings.scaleY);
		projection = new OffsetProjectionTransform(projection, this.settings.offsetX, this.settings.offsetY);
		return projection;
	}

	public GeographicProjection getNormalizedProjection() {
		return GeographicProjection.orientProjection(
				GeographicProjection.projections.get(this.settings.projection), GeographicProjection.Orientation.upright);
	}

	//json template to be filled by Gson
	public static class JsonSettings {
		public String projection = "equirectangular";
		public GeographicProjection.Orientation orentation = GeographicProjection.Orientation.swapped; // This typo is unfortunate, but let's keep it for backward compatibility
		public double scaleX = 100000.0d;
		public double scaleY = 100000.0d;
		public double offsetX = 0;
		public double offsetY = 0;
		public boolean smoothblend = true;
		public boolean roads = true;
		public String customcubic = "";
		public boolean dynamicbaseheight = true;
		@Deprecated
		public boolean osmwater = true;
		public boolean buildings = true;
	}
}