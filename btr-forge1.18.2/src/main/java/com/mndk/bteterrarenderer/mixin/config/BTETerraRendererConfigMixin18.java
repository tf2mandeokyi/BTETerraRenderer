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

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite public boolean isDoRender() {
        return BTETerraRendererConfigImpl18.doRender;
    }
    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite public void setDoRender(boolean doRender) {
        BTETerraRendererConfigImpl18.doRender = doRender;
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite public String getMapServiceCategory() {
        return BTETerraRendererConfigImpl18.mapServiceCategory;
    }
    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite public void setMapServiceCategory(String mapServiceCategory) {
        BTETerraRendererConfigImpl18.mapServiceCategory = mapServiceCategory;
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite public String getMapServiceId() {
        return BTETerraRendererConfigImpl18.mapServiceId;
    }
    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite public void setMapServiceId(String mapServiceId) {
        BTETerraRendererConfigImpl18.mapServiceId = mapServiceId;
    }

    @Mixin(value = BTETerraRendererConfig.HologramConfig.class, remap = false)
    public static class HologramConfigMixin {
        /** @author m4ndeokyi
         *  @reason mixin overwrite */
        @Overwrite public double getXAlign() {
            return BTETerraRendererConfigImpl18.HOLOGRAM_CONFIG.xAlign;
        }
        /** @author m4ndeokyi
         *  @reason mixin overwrite */
        @Overwrite public void setXAlign(double xAlign) {
            BTETerraRendererConfigImpl18.HOLOGRAM_CONFIG.xAlign = xAlign;
        }

        /** @author m4ndeokyi
         *  @reason mixin overwrite */
        @Overwrite public double getYAlign() {
            return BTETerraRendererConfigImpl18.HOLOGRAM_CONFIG.yAlign;
        }
        /** @author m4ndeokyi
         *  @reason mixin overwrite */
        @Overwrite public void setYAlign(double yAlign) {
            BTETerraRendererConfigImpl18.HOLOGRAM_CONFIG.yAlign = yAlign;
        }

        /** @author m4ndeokyi
         *  @reason mixin overwrite */
        @Overwrite public double getZAlign() {
            return BTETerraRendererConfigImpl18.HOLOGRAM_CONFIG.zAlign;
        }
        /** @author m4ndeokyi
         *  @reason mixin overwrite */
        @Overwrite public void setZAlign(double zAlign) {
            BTETerraRendererConfigImpl18.HOLOGRAM_CONFIG.zAlign = zAlign;
        }

        /** @author m4ndeokyi
         *  @reason mixin overwrite */
        @Overwrite public boolean isLockNorth() {
            return BTETerraRendererConfigImpl18.HOLOGRAM_CONFIG.lockNorth;
        }
        /** @author m4ndeokyi
         *  @reason mixin overwrite */
        @Overwrite public void setLockNorth(boolean lockNorth) {
            BTETerraRendererConfigImpl18.HOLOGRAM_CONFIG.lockNorth = lockNorth;
        }

        /** @author m4ndeokyi
         *  @reason mixin overwrite */
        @Overwrite public double getFlatMapYAxis() {
            return BTETerraRendererConfigImpl18.HOLOGRAM_CONFIG.flatMapYAxis;
        }
        /** @author m4ndeokyi
         *  @reason mixin overwrite */
        @Overwrite public void setFlatMapYAxis(double flatMapYAxis) {
            BTETerraRendererConfigImpl18.HOLOGRAM_CONFIG.flatMapYAxis = flatMapYAxis;
        }

        /** @author m4ndeokyi
         *  @reason mixin overwrite */
        @Overwrite public double getOpacity() {
            return BTETerraRendererConfigImpl18.HOLOGRAM_CONFIG.opacity;
        }
        /** @author m4ndeokyi
         *  @reason mixin overwrite */
        @Overwrite public void setOpacity(double opacity) {
            BTETerraRendererConfigImpl18.HOLOGRAM_CONFIG.opacity = opacity;
        }

        /** @author m4ndeokyi
         *  @reason mixin overwrite */
        @Overwrite public double getYDiffLimit() {
            return BTETerraRendererConfigImpl18.HOLOGRAM_CONFIG.yDiffLimit;
        }
        /** @author m4ndeokyi
         *  @reason mixin overwrite */
        @Overwrite public void setYDiffLimit(double yDiffLimit) {
            BTETerraRendererConfigImpl18.HOLOGRAM_CONFIG.yDiffLimit = yDiffLimit;
        }
    }

    @Mixin(value = BTETerraRendererConfig.UIConfig.class, remap = false)
    public static class UIConfigMixin {
        /** @author m4ndeokyi
         *  @reason mixin overwrite */
        @Overwrite public SidebarSide getSidebarSide() {
            return BTETerraRendererConfigImpl18.UI_CONFIG.sidebarSide;
        }
        /** @author m4ndeokyi
         *  @reason mixin overwrite */
        @Overwrite public void setSidebarSide(SidebarSide side) {
            BTETerraRendererConfigImpl18.UI_CONFIG.sidebarSide = side;
        }

        /** @author m4ndeokyi
         *  @reason mixin overwrite */
        @Overwrite public double getSidebarWidth() {
            return BTETerraRendererConfigImpl18.UI_CONFIG.sidebarWidth;
        }
        /** @author m4ndeokyi
         *  @reason mixin overwrite */
        @Overwrite public void setSidebarWidth(double sidebarWidth) {
            BTETerraRendererConfigImpl18.UI_CONFIG.sidebarWidth = sidebarWidth;
        }

        /** @author m4ndeokyi
         *  @reason mixin overwrite */
        @Overwrite public double getSidebarOpacity() {
            return BTETerraRendererConfigImpl18.UI_CONFIG.sidebarOpacity;
        }
        /** @author m4ndeokyi
         *  @reason mixin overwrite */
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
