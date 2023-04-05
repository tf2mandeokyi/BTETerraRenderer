package com.mndk.bteterrarenderer.connector;

import com.mndk.bteterrarenderer.connector.minecraft.IResourceLocation;
import com.mndk.bteterrarenderer.connector.minecraft.IResourceLocationImpl;
import com.mndk.bteterrarenderer.connector.minecraft.gui.*;
import com.mndk.bteterrarenderer.connector.terraplusplus.projection.IGeographicProjection;
import com.mndk.bteterrarenderer.connector.terraplusplus.projection.IGeographicProjectionImpl;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;

@ConnectorImpl
public class DependencyConnectorSupplierImpl implements DependencyConnectorSupplier {

    public IGuiChat newGuiChat() {
        return new IGuiChatImpl();
    }

    public IGuiButton newGuiButton(int componentId, int x, int y, int width, int height, String buttonText) {
        return new IGuiButtonImpl(componentId, x, y, width, height, buttonText);
    }

    public IGuiSlider newGuiSlider(int componentId,
                                   int x, int y, int width, int height,
                                   String prefix, String suffix,
                                   double minValue, double maxValue, double currentValue,
                                   boolean showDecimal, boolean drawString,
                                   IGuiSlider.SliderChangeHandler par) {
        return new IGuiSliderImpl(
                componentId, x, y, width, height,
                prefix, suffix, minValue, maxValue, currentValue,
                showDecimal, drawString, par);
    }

    public IGuiCheckBox newCheckBox(int componentId, int x, int y, String displayString, boolean isChecked) {
        return new IGuiCheckBoxImpl(componentId, x, y, displayString, isChecked);
    }

    public IGuiTextField newGuiTextField(int componentId, IFontRenderer fontRenderer,
                                         int x, int y, int width, int height) {

        return new IGuiTextFieldImpl(componentId, ((IFontRendererImpl) fontRenderer).getFontRenderer(), x, y, width, height);
    }

    public IResourceLocation newResourceLocation(String modId, String location) {
        return new IResourceLocationImpl(new ResourceLocation(modId, location));
    }

    @Override
    public String projectionToJson(IGeographicProjection projection) throws IOException {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public void registerProjection(String id, Class<? extends IGeographicProjection> projection) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public IGeographicProjectionImpl parse(String projectionJson) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public IGeographicProjectionImpl getBTEProjection() {
        throw new UnsupportedOperationException("TODO");
    }
}