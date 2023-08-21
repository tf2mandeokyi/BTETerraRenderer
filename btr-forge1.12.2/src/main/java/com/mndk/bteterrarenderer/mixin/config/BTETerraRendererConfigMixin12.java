package com.mndk.bteterrarenderer.mixin.config;

import com.mndk.bteterrarenderer.core.config.BTETerraRendererConfig;
import com.mndk.bteterrarenderer.core.gui.sidebar.SidebarSide;
import com.mndk.bteterrarenderer.mod.config.BTETerraRendererConfigImpl12;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BTETerraRendererConfig.class, remap = false)
public class BTETerraRendererConfigMixin12 {

    @Overwrite public boolean isDoRender() {
        return BTETerraRendererConfigImpl12.doRender;
    }
    @Overwrite public void setDoRender(boolean doRender) {
        BTETerraRendererConfigImpl12.doRender = doRender;
    }

    @Overwrite public String getMapServiceCategory() {
        return BTETerraRendererConfigImpl12.mapServiceCategory;
    }
    @Overwrite public void setMapServiceCategory(String mapServiceCategory) {
        BTETerraRendererConfigImpl12.mapServiceCategory = mapServiceCategory;
    }

    @Overwrite public String getMapServiceId() {
        return BTETerraRendererConfigImpl12.mapServiceId;
    }
    @Overwrite public void setMapServiceId(String mapServiceId) {
        BTETerraRendererConfigImpl12.mapServiceId = mapServiceId;
    }

    @Mixin(value = BTETerraRendererConfig.HologramConfig.class, remap = false)
    public static class HologramConfigMixin {
        @Overwrite public double getXAlign() {
            return BTETerraRendererConfigImpl12.HOLOGRAM_CONFIG.xAlign;
        }
        @Overwrite public void setXAlign(double xAlign) {
            BTETerraRendererConfigImpl12.HOLOGRAM_CONFIG.xAlign = xAlign;
        }
        @Overwrite public double getYAlign() {
            return BTETerraRendererConfigImpl12.HOLOGRAM_CONFIG.yAlign;
        }
        @Overwrite public void setYAlign(double yAlign) {
            BTETerraRendererConfigImpl12.HOLOGRAM_CONFIG.yAlign = yAlign;
        }
        @Overwrite public double getZAlign() {
            return BTETerraRendererConfigImpl12.HOLOGRAM_CONFIG.zAlign;
        }
        @Overwrite public void setZAlign(double zAlign) {
            BTETerraRendererConfigImpl12.HOLOGRAM_CONFIG.zAlign = zAlign;
        }
        @Overwrite public boolean isLockNorth() {
            return BTETerraRendererConfigImpl12.HOLOGRAM_CONFIG.lockNorth;
        }
        @Overwrite public void setLockNorth(boolean lockNorth) {
            BTETerraRendererConfigImpl12.HOLOGRAM_CONFIG.lockNorth = lockNorth;
        }
        @Overwrite public double getFlatMapYAxis() {
            return BTETerraRendererConfigImpl12.HOLOGRAM_CONFIG.flatMapYAxis;
        }
        @Overwrite public void setFlatMapYAxis(double flatMapYAxis) {
            BTETerraRendererConfigImpl12.HOLOGRAM_CONFIG.flatMapYAxis = flatMapYAxis;
        }
        @Overwrite public double getOpacity() {
            return BTETerraRendererConfigImpl12.HOLOGRAM_CONFIG.opacity;
        }
        @Overwrite public void setOpacity(double opacity) {
            BTETerraRendererConfigImpl12.HOLOGRAM_CONFIG.opacity = opacity;
        }
        @Overwrite public double getYDiffLimit() {
            return BTETerraRendererConfigImpl12.HOLOGRAM_CONFIG.yDiffLimit;
        }
        @Overwrite public void setYDiffLimit(double yDiffLimit) {
            BTETerraRendererConfigImpl12.HOLOGRAM_CONFIG.yDiffLimit = yDiffLimit;
        }
    }

    @Mixin(value = BTETerraRendererConfig.UIConfig.class, remap = false)
    public static class UIConfigMixin {
        @Overwrite public SidebarSide getSidebarSide() {
            return BTETerraRendererConfigImpl12.UI_CONFIG.sidebarSide;
        }
        @Overwrite public void setSidebarSide(SidebarSide side) {
            BTETerraRendererConfigImpl12.UI_CONFIG.sidebarSide = side;
        }
        @Overwrite public double getSidebarWidth() {
            return BTETerraRendererConfigImpl12.UI_CONFIG.sidebarWidth;
        }
        @Overwrite public void setSidebarWidth(double sidebarWidth) {
            BTETerraRendererConfigImpl12.UI_CONFIG.sidebarWidth = sidebarWidth;
        }
        @Overwrite public double getSidebarOpacity() {
            return BTETerraRendererConfigImpl12.UI_CONFIG.sidebarOpacity;
        }
        @Overwrite public void setSidebarOpacity(double sidebarOpacity) {
            BTETerraRendererConfigImpl12.UI_CONFIG.sidebarOpacity = sidebarOpacity;
        }
    }

    @Inject(method = "save", at = @At("HEAD"))
    public void onSave(CallbackInfo ci) {
        BTETerraRendererConfigImpl12.saveConfig();
    }

}
