package com.mndk.bteterrarenderer.core.tile;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.mndk.bteterrarenderer.dep.terraplusplus.projection.GeographicProjection;
import com.mndk.bteterrarenderer.mcconnector.i18n.Translatable;
import com.mndk.bteterrarenderer.util.concurrent.CacheStorage;
import com.mndk.bteterrarenderer.util.json.JsonString;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TileMapServiceCommonProperties {

    private Translatable<String> name;
    private String tileUrl;
    private int nThreads;
    @Nullable
    private URL hudImageUrl;
    @Nullable
    private URL iconUrl;
    @Nullable
    private Translatable<JsonString> copyrightTextJson;
    @Nullable
    private GeographicProjection hologramProjection;
    @Nullable
    private CacheStorage.Config cacheConfig;

    @JsonCreator
    public TileMapServiceCommonProperties(
            @JsonProperty(value = "name", required = true) Translatable<String> name,
            @JsonProperty(value = "tile_url", required = true) String tileUrl,
            @Nullable @JsonProperty("max_thread") Integer nThreads,
            @Nullable @JsonProperty("copyright") Translatable<JsonString> copyrightTextJson,
            @Nullable @JsonProperty("icon_url") URL iconUrl,
            @Nullable @JsonProperty("hud_image") URL hudImageUrl,
            @Nullable @JsonProperty("hologram_projection") GeographicProjection hologramProjection,
            @Nullable @JsonProperty("cache") CacheStorage.Config cacheConfig
    ) {
        this.name = name;
        this.copyrightTextJson = copyrightTextJson;
        this.tileUrl = tileUrl;
        this.iconUrl = iconUrl;
        this.hudImageUrl = hudImageUrl;
        this.nThreads = nThreads != null ? nThreads : AbstractTileMapService.DEFAULT_MAX_THREAD;
        this.hologramProjection = hologramProjection;
        this.cacheConfig = cacheConfig;
    }

    void write(JsonGenerator gen) throws IOException {
        gen.writeObjectField("name", this.name);
        gen.writeStringField("tile_url", this.tileUrl);
        if (this.iconUrl != null) {
            gen.writeStringField("icon_url", this.iconUrl.toString());
        }
        if (this.hudImageUrl != null) {
            gen.writeStringField("hud_image", this.hudImageUrl.toString());
        }
        gen.writeNumberField("max_thread", this.nThreads);
        gen.writeObjectField("copyright", this.copyrightTextJson);
        gen.writeObjectField("hologram_projection", this.hologramProjection);
    }

    static TileMapServiceCommonProperties from(AbstractTileMapService<?> tms) {
        TileMapServiceCommonProperties result = new TileMapServiceCommonProperties();
        result.name = tms.getName();
        result.tileUrl = tms.getDummyTileUrl();
        result.iconUrl = tms.getIconUrl();
        result.hudImageUrl = tms.getHudImageUrl();
        result.nThreads = tms.getNThreads();
        result.copyrightTextJson = Optional.ofNullable(tms.getCopyrightTextJson())
                .map(json -> json.map(JsonString::fromUnsafe))
                .orElse(null);
        result.hologramProjection = tms.getHologramProjection();

        return result;
    }
}
