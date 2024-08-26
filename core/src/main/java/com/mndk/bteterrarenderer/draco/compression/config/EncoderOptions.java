package com.mndk.bteterrarenderer.draco.compression.config;

public class EncoderOptions extends EncoderOptionsBase<Integer> {

    protected EncoderOptions() {}

    public static EncoderOptions createDefaultOptions() {
        EncoderOptions options = new EncoderOptions();
        options.setSupportedFeature(EncodingFeatures.EDGE_BREAKER, true);
        options.setSupportedFeature(EncodingFeatures.PREDICTIVE_EDGE_BREAKER, true);
        return options;
    }

    public static EncoderOptions createEmptyOptions() {
        return new EncoderOptions();
    }

}
