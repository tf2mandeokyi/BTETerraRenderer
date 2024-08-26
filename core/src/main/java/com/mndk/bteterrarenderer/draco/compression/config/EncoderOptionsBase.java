package com.mndk.bteterrarenderer.draco.compression.config;

import com.mndk.bteterrarenderer.draco.core.Options;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EncoderOptionsBase<K> extends DracoOptions<K> {

    public static <K> EncoderOptionsBase<K> createDefaultOptions() {
        EncoderOptionsBase<K> options = new EncoderOptionsBase<>();
        options.setSupportedFeature(EncodingFeatures.EDGE_BREAKER, true);
        options.setSupportedFeature(EncodingFeatures.PREDICTIVE_EDGE_BREAKER, true);
        return options;
    }

    public static <K> EncoderOptionsBase<K> createEmptyOptions() {
        return new EncoderOptionsBase<>();
    }

    private Options featureOptions = new Options();

    protected EncoderOptionsBase() {}

    public int getEncodingSpeed() {
        return this.getGlobalInt("encoding_speed", 5);
    }

    public int getDecodingSpeed() {
        return this.getGlobalInt("decoding_speed", 5);
    }

    public int getSpeed() {
        int encodingSpeed = this.getGlobalInt("encoding_speed", -1);
        int decodingSpeed = this.getGlobalInt("decoding_speed", -1);
        int maxSpeed = Math.max(encodingSpeed, decodingSpeed);
        return maxSpeed == -1 ? 5 : maxSpeed;
    }

    public void setSpeed(int encodingSpeed, int decodingSpeed) {
        this.setGlobalInt("encoding_speed", encodingSpeed);
        this.setGlobalInt("decoding_speed", decodingSpeed);
    }

    public boolean isSpeedSet() {
        return this.isGlobalOptionSet("encoding_speed") || this.isGlobalOptionSet("decoding_speed");
    }

    public void setSupportedFeature(String name, boolean supported) {
        featureOptions.setBool(name, supported);
    }
    public boolean isFeatureSupported(String name) {
        return featureOptions.getBool(name);
    }

}