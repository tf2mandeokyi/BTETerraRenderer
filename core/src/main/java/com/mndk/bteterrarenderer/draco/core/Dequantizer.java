package com.mndk.bteterrarenderer.draco.core;

/**
 * Class for dequantizing values that were previously quantized using the
 * {@link Quantizer} class.
 */
public class Dequantizer {

    private float delta;

    public Dequantizer() {
        this.delta = 1.0f;
    }

    /**
    * Initializes the dequantizer. Both parameters must correspond to the values
    * provided to the initializer of the {@link Quantizer} class.
    */
    public Status init(float range, int maxQuantizedValue) {
        if(maxQuantizedValue <= 0) {
            return new Status(Status.Code.INVALID_PARAMETER, "max_quantized_value must be greater than 0");
        }
        this.delta = range / maxQuantizedValue;
        return Status.OK;
    }

    /** Initializes the dequantizer using the {@code delta} between two quantized values. */
    public boolean init(float delta) {
        this.delta = delta;
        return true;
    }

    public float dequantizeFloat(int val) {
        return val * delta;
    }
}
