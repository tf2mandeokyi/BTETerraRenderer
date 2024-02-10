package com.mndk.bteterrarenderer.core.gui.mcfx.list;

@FunctionalInterface
public interface HListWidthFunction {
    double apply(int totalWidth, double widthLeft);

    static HListWidthFunction percent(double percentage) {
        return (totalWidth, widthLeft) -> percentage * totalWidth / 100;
    }
}
