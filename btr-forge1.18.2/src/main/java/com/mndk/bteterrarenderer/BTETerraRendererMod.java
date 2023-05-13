package com.mndk.bteterrarenderer;

import com.mndk.bteterrarenderer.config.BTRConfigConnectorImpl;
import com.mndk.bteterrarenderer.connector.DependencyConnectorSupplierImpl;
import com.mndk.bteterrarenderer.connector.ImplFinder;
import com.mndk.bteterrarenderer.connector.graphics.GraphicsConnectorImpl;
import com.mndk.bteterrarenderer.connector.graphics.TileGraphicsConnectorImpl;
import com.mndk.bteterrarenderer.connector.gui.GuiStaticConnectorImpl;
import com.mndk.bteterrarenderer.connector.minecraft.*;
import com.mndk.bteterrarenderer.connector.terraplusplus.HttpConnectorImpl;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.logging.log4j.LogManager;

@Mod(BTETerraRendererConstants.MODID)
public class BTETerraRendererMod {
    public BTETerraRendererMod() {
        ModLoadingContext modLoadingContext = ModLoadingContext.get();
        modLoadingContext.registerConfig(ModConfig.Type.CLIENT, BTRConfigConnectorImpl.SPEC);
    }

    static {
        BTETerraRendererConstants.LOGGER = LogManager.getLogger(BTETerraRendererConstants.class);
        ImplFinder.add(
                BTRConfigConnectorImpl.class,
                TileGraphicsConnectorImpl.class,
                GraphicsConnectorImpl.class,
                GuiStaticConnectorImpl.class,
                DependencyConnectorSupplierImpl.class,
                ClientPlayerConnectorImpl.class,
                ErrorMessageHandlerImpl.class,
                I18nConnectorImpl.class,
                KeyBindingsConnectorImpl.class,
                MouseConnectorImpl.class,
                SoundConnectorImpl.class,
                HttpConnectorImpl.class
        );
    }
}
