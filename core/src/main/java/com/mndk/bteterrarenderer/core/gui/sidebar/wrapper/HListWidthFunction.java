package com.mndk.bteterrarenderer.core.gui.sidebar.wrapper;

@FunctionalInterface
public interface HListWidthFunction {
    double apply(int totalWidth, double widthLeft);

    static HListWidthFunction percent(double percentage) {
        return (totalWidth, widthLeft) -> percentage * totalWidth / 100;
    }
}
