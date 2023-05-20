package com.mndk.bteterrarenderer;

import com.mndk.bteterrarenderer.config.BTRConfigConnector;
import com.mndk.bteterrarenderer.config.BTRConfigConnectorImpl;
import com.mndk.bteterrarenderer.connector.DependencyConnectorSupplierImpl;
import com.mndk.bteterrarenderer.connector.ImplFinder;
import com.mndk.bteterrarenderer.connector.graphics.GraphicsConnectorImpl;
import com.mndk.bteterrarenderer.connector.graphics.TileGraphicsConnectorImpl;
import com.mndk.bteterrarenderer.connector.gui.FontConnectorImpl;
import com.mndk.bteterrarenderer.connector.gui.GuiStaticConnectorImpl;
import com.mndk.bteterrarenderer.connector.minecraft.GameInputConnectorImpl;
import com.mndk.bteterrarenderer.connector.minecraft.I18nConnectorImpl;
import com.mndk.bteterrarenderer.connector.minecraft.MinecraftClientConnectorImpl;
import com.mndk.bteterrarenderer.connector.terraplusplus.HttpConnectorImpl;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.logging.log4j.LogManager;

@Mod(BTETerraRendererConstants.MODID)
public class BTETerraRendererMod {
    public BTETerraRendererMod() {
        ModLoadingContext modLoadingContext = ModLoadingContext.get();
        modLoadingContext.registerConfig(ModConfig.Type.CLIENT, BTRConfigConnectorImpl.CONFIG_SPEC);
        BTRConfigConnector.load();
    }

    static {
        BTETerraRendererConstants.LOGGER = LogManager.getLogger(BTETerraRendererConstants.class);
        ImplFinder.add(
                BTRConfigConnectorImpl.class,
                TileGraphicsConnectorImpl.class,
                GraphicsConnectorImpl.class,
                GuiStaticConnectorImpl.class,
                DependencyConnectorSupplierImpl.class,
                MinecraftClientConnectorImpl.class,
                FontConnectorImpl.class,
                I18nConnectorImpl.class,
                HttpConnectorImpl.class,
                GameInputConnectorImpl.class
        );
    }
}
