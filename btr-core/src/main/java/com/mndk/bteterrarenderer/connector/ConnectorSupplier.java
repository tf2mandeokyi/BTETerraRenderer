package com.mndk.bteterrarenderer.connector;

import com.mndk.bteterrarenderer.connector.minecraft.ResourceLocationConnector;
import com.mndk.bteterrarenderer.connector.minecraft.gui.*;

public interface ConnectorSupplier {
    GuiChatConnector newGuiChat();

    GuiButtonConnector newGuiButton(int componentId, int x, int y, int width, int height, String buttonText);
    GuiSliderConnector newGuiSlider(int componentId,
                                    int x, int y, int width, int height,
                                    String prefix, String suffix,
                                    double minValue, double maxValue, double currentValue,
                                    boolean showDecimal, boolean drawString,
                                    GuiSliderConnector.SliderChangeHandler par);
    GuiCheckBoxConnector newCheckBox(int componentId, int x, int y, String displayString, boolean isChecked);
    GuiTextFieldConnector newGuiTextField(int componentId, FontRendererConnector fontRenderer,
                                          int x, int y, int width, int height);
    ResourceLocationConnector newResourceLocation(String modId, String location);
}
