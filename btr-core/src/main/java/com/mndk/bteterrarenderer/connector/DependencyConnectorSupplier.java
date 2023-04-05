package com.mndk.bteterrarenderer.connector;

import com.mndk.bteterrarenderer.connector.gui.*;
import com.mndk.bteterrarenderer.connector.minecraft.IResourceLocation;
import com.mndk.bteterrarenderer.connector.terraplusplus.projection.IGeographicProjection;

import java.io.IOException;

public interface DependencyConnectorSupplier {
    DependencyConnectorSupplier INSTANCE = ImplFinder.search(DependencyConnectorSupplier.class);

    // Minecraft
    IGuiChat newGuiChat();
    IGuiButton newGuiButton(int componentId, int x, int y, int width, int height, String buttonText);
    IGuiSlider newGuiSlider(int componentId,
                            int x, int y, int width, int height,
                            String prefix, String suffix,
                            double minValue, double maxValue, double currentValue,
                            boolean showDecimal, boolean drawString,
                            IGuiSlider.SliderChangeHandler par);
    IGuiCheckBox newCheckBox(int componentId, int x, int y, String displayString, boolean isChecked);
    IGuiTextField newGuiTextField(int componentId, IFontRenderer fontRenderer,
                                  int x, int y, int width, int height);
    IResourceLocation newResourceLocation(String modId, String location);

    // Terraplusplus
    String projectionToJson(IGeographicProjection projection) throws IOException;
    IGeographicProjection parse(String projectionJson);
    IGeographicProjection createBTEProjection();
}
