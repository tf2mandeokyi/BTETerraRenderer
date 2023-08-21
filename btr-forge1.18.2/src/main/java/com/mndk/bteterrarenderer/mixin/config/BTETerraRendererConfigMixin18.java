package com.mndk.bteterrarenderer.mixin.config;

import com.mndk.bteterrarenderer.core.config.BTETerraRendererConfig;
import com.mndk.bteterrarenderer.core.gui.sidebar.SidebarSide;
import com.mndk.bteterrarenderer.mod.config.BTETerraRendererConfigImpl18;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BTETerraRendererConfig.class, remap = false)
public class BTETerraRendererConfigMixin18 {

    @Overwrite
    public boolean isDoRender() {
        return BTETerraRendererConfigImpl18.doRender;
    }
    @Overwrite public void setDoRender(boolean doRender) {
        BTETerraRendererConfigImpl18.doRender = doRender;
    }

    @Overwrite public String getMapServiceCategory() {
        return BTETerraRendererConfigImpl18.mapServiceCategory;
    }
    @Overwrite public void setMapServiceCategory(String mapServiceCategory) {
        BTETerraRendererConfigImpl18.mapServiceCategory = mapServiceCategory;
    }

    @Overwrite public String getMapServiceId() {
        return BTETerraRendererConfigImpl18.mapServiceId;
    }
    @Overwrite public void setMapServiceId(String mapServiceId) {
        BTETerraRendererConfigImpl18.mapServiceId = mapServiceId;
    }

    @Mixin(value = BTETerraRendererConfig.HologramConfig.class, remap = false)
    public static class HologramConfigMixin {
        @Overwrite public double getXAlign() {
            return BTETerraRendererConfigImpl18.HOLOGRAM_CONFIG.xAlign;
        }
        @Overwrite public void setXAlign(double xAlign) {
            BTETerraRendererConfigImpl18.HOLOGRAM_CONFIG.xAlign = xAlign;
        }
        @Overwrite public double getYAlign() {
            return BTETerraRendererConfigImpl18.HOLOGRAM_CONFIG.yAlign;
        }
        @Overwrite public void setYAlign(double yAlign) {
            BTETerraRendererConfigImpl18.HOLOGRAM_CONFIG.yAlign = yAlign;
        }
        @Overwrite public double getZAlign() {
            return BTETerraRendererConfigImpl18.HOLOGRAM_CONFIG.zAlign;
        }
        @Overwrite public void setZAlign(double zAlign) {
            BTETerraRendererConfigImpl18.HOLOGRAM_CONFIG.zAlign = zAlign;
        }
        @Overwrite public boolean isLockNorth() {
            return BTETerraRendererConfigImpl18.HOLOGRAM_CONFIG.lockNorth;
        }
        @Overwrite public void setLockNorth(boolean lockNorth) {
            BTETerraRendererConfigImpl18.HOLOGRAM_CONFIG.lockNorth = lockNorth;
        }
        @Overwrite public double getFlatMapYAxis() {
            return BTETerraRendererConfigImpl18.HOLOGRAM_CONFIG.flatMapYAxis;
        }
        @Overwrite public void setFlatMapYAxis(double flatMapYAxis) {
            BTETerraRendererConfigImpl18.HOLOGRAM_CONFIG.flatMapYAxis = flatMapYAxis;
        }
        @Overwrite public double getOpacity() {
            return BTETerraRendererConfigImpl18.HOLOGRAM_CONFIG.opacity;
        }
        @Overwrite public void setOpacity(double opacity) {
            BTETerraRendererConfigImpl18.HOLOGRAM_CONFIG.opacity = opacity;
        }
        @Overwrite public double getYDiffLimit() {
            return BTETerraRendererConfigImpl18.HOLOGRAM_CONFIG.yDiffLimit;
        }
        @Overwrite public void setYDiffLimit(double yDiffLimit) {
            BTETerraRendererConfigImpl18.HOLOGRAM_CONFIG.yDiffLimit = yDiffLimit;
        }
    }

    @Mixin(value = BTETerraRendererConfig.UIConfig.class, remap = false)
    public static class UIConfigMixin {
        @Overwrite public SidebarSide getSidebarSide() {
            return BTETerraRendererConfigImpl18.UI_CONFIG.sidebarSide;
        }
        @Overwrite public void setSidebarSide(SidebarSide side) {
            BTETerraRendererConfigImpl18.UI_CONFIG.sidebarSide = side;
        }
        @Overwrite public double getSidebarWidth() {
            return BTETerraRendererConfigImpl18.UI_CONFIG.sidebarWidth;
        }
        @Overwrite public void setSidebarWidth(double sidebarWidth) {
            BTETerraRendererConfigImpl18.UI_CONFIG.sidebarWidth = sidebarWidth;
        }
        @Overwrite public double getSidebarOpacity() {
            return BTETerraRendererConfigImpl18.UI_CONFIG.sidebarOpacity;
        }
        @Overwrite public void setSidebarOpacity(double sidebarOpacity) {
            BTETerraRendererConfigImpl18.UI_CONFIG.sidebarOpacity = sidebarOpacity;
        }
    }

    @Inject(method = "save", at = @At("HEAD"))
    public void onSave(CallbackInfo ci) {
        BTETerraRendererConfigImpl18.saveConfig();
    }

    @Inject(method = "load", at = @At("HEAD"))
    public void onLoad(CallbackInfo ci) {
        BTETerraRendererConfigImpl18.readConfig();
    }
}
