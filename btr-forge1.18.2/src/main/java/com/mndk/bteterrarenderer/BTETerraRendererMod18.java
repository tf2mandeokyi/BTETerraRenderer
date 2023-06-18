package com.mndk.bteterrarenderer;

import com.mndk.bteterrarenderer.config.BTRConfigConnectorImpl18;
import com.mndk.bteterrarenderer.connector.DependencyConnectorSupplierImpl18;
import com.mndk.bteterrarenderer.connector.ImplFinder;
import com.mndk.bteterrarenderer.connector.graphics.GraphicsConnectorImpl18;
import com.mndk.bteterrarenderer.connector.graphics.ModelGraphicsConnectorImpl18;
import com.mndk.bteterrarenderer.connector.gui.FontConnectorImpl18;
import com.mndk.bteterrarenderer.connector.gui.GuiStaticConnectorImpl18;
import com.mndk.bteterrarenderer.connector.minecraft.GameInputConnectorImpl18;
import com.mndk.bteterrarenderer.connector.minecraft.I18nConnectorImpl18;
import com.mndk.bteterrarenderer.connector.minecraft.MinecraftClientConnectorImpl18;
import com.mndk.bteterrarenderer.connector.terraplusplus.HttpConnectorImpl18;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;

@Mod(BTETerraRendererConstants.MODID)
public class BTETerraRendererMod18 {
    public BTETerraRendererMod18() {
        BTRConfigConnectorImpl18.register();
    }

    static {
        BTETerraRendererConstants.LOGGER = LogManager.getLogger(BTETerraRendererMod18.class);
        ImplFinder.add(
                BTRConfigConnectorImpl18.class,
                ModelGraphicsConnectorImpl18.class,
                GraphicsConnectorImpl18.class,
                GuiStaticConnectorImpl18.class,
                DependencyConnectorSupplierImpl18.class,
                MinecraftClientConnectorImpl18.class,
                FontConnectorImpl18.class,
                I18nConnectorImpl18.class,
                HttpConnectorImpl18.class,
                GameInputConnectorImpl18.class
        );
    }
}
