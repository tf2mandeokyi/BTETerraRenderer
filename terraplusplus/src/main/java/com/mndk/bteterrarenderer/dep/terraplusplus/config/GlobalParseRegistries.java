package com.mndk.bteterrarenderer.dep.terraplusplus.config;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mndk.bteterrarenderer.dep.terraplusplus.projection.*;
import com.mndk.bteterrarenderer.dep.terraplusplus.projection.dymaxion.BTEDymaxionProjection;
import com.mndk.bteterrarenderer.dep.terraplusplus.projection.dymaxion.ConformalDynmaxionProjection;
import com.mndk.bteterrarenderer.dep.terraplusplus.projection.dymaxion.DymaxionProjection;
import com.mndk.bteterrarenderer.dep.terraplusplus.projection.mercator.CenteredMercatorProjection;
import com.mndk.bteterrarenderer.dep.terraplusplus.projection.mercator.TransverseMercatorProjection;
import com.mndk.bteterrarenderer.dep.terraplusplus.projection.mercator.WebMercatorProjection;
import com.mndk.bteterrarenderer.dep.terraplusplus.projection.transform.*;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;

/**
 * Identifies implementation classes by their type names.
 * <p>
 * Warning: modifying any of the fields in this class outside of initialization time may cause unexpected behavior.
 *
 * @author DaPorkchop_
 */
@UtilityClass
public class GlobalParseRegistries {
    public final BiMap<String, Class<? extends GeographicProjection>> PROJECTIONS = new BiMapBuilder<String, Class<? extends GeographicProjection>>()
            //normal projections
            .put("centered_mercator", CenteredMercatorProjection.class)
            .put("web_mercator", WebMercatorProjection.class)
            .put("transverse_mercator", TransverseMercatorProjection.class)
            .put("equirectangular", EquirectangularProjection.class)
            .put("sinusoidal", SinusoidalProjection.class)
            .put("equal_earth", EqualEarthProjection.class)
            .put("bte_conformal_dymaxion", BTEDymaxionProjection.class)
            .put("dymaxion", DymaxionProjection.class)
            .put("conformal_dymaxion", ConformalDynmaxionProjection.class)
            .put("lambert_azimuthal", LambertAzimuthalProjection.class)
            .put("azimuthal_equidistant", AzimuthalEquidistantProjection.class)
            .put("stereographic", StereographicProjection.class)
            //transformations
            .put("clamp", ClampProjectionTransform.class)
            .put("flip_horizontal", FlipHorizontalProjectionTransform.class)
            .put("flip_vertical", FlipVerticalProjectionTransform.class)
            .put("offset", OffsetProjectionTransform.class)
            .put("rotate", RotateProjectionTransform.class)
            .put("scale", ScaleProjectionTransform.class)
            .put("swap_axes", SwapAxesProjectionTransform.class)
            .build();

    /**
     * Stupid builder class so that I can populate the initial values cleanly using chained method calls.
     *
     * @author DaPorkchop_
     */
    @RequiredArgsConstructor
    private static class BiMapBuilder<K, V> {
        @NonNull
        private final BiMap<K, V> delegate;

        public BiMapBuilder() {
            this(HashBiMap.create());
        }

        public BiMapBuilder<K, V> put(K key, V value) {
            this.delegate.put(key, value);
            return this;
        }

        public BiMap<K, V> build() {
            return this.delegate;
        }
    }
}
