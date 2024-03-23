package com.mndk.bteterrarenderer.mcconnector.client.mcfx.list;

import com.mndk.bteterrarenderer.core.util.accessor.PropertyAccessor;

@FunctionalInterface
public interface WidthFunction {
    double apply(int totalWidth, double widthLeft);

    static WidthFunction px(double px) {
        return (totalWidth, widthLeft) -> px;
    }

    static WidthFunction px(PropertyAccessor<Double> px) {
        return (totalWidth, widthLeft) -> px.get();
    }
}
