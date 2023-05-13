package com.mndk.bteterrarenderer.connector;

import com.mndk.bteterrarenderer.connector.gui.IGuiChat;
import com.mndk.bteterrarenderer.connector.gui.IGuiChatImpl;
import com.mndk.bteterrarenderer.connector.gui.IGuiTextField;
import com.mndk.bteterrarenderer.connector.gui.IGuiTextFieldImpl;
import com.mndk.bteterrarenderer.connector.minecraft.IResourceLocation;
import com.mndk.bteterrarenderer.connector.minecraft.IResourceLocationImpl;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;

@ConnectorImpl
@SuppressWarnings("unused")
public class DependencyConnectorSupplierImpl implements DependencyConnectorSupplier {

    private static final IResourceLocation BUTTON_TEXTURES = new IResourceLocationImpl(new DummyGuiButton().getButtonTextures());

    public IGuiChat newGuiChat() {
        return new IGuiChatImpl();
    }

    public IGuiTextField newGuiTextField(int componentId, int x, int y, int width, int height) {

        return new IGuiTextFieldImpl(componentId, x, y, width, height);
    }

    public IResourceLocation newResourceLocation(String modId, String location) {
        return new IResourceLocationImpl(new ResourceLocation(modId, location));
    }

    @Override
    public IResourceLocation getWidgetTextures() {
        return BUTTON_TEXTURES;
    }

    private static class DummyGuiButton extends GuiButton {
        public DummyGuiButton() { super(0, 0, 0, ""); }
        public ResourceLocation getButtonTextures() { return BUTTON_TEXTURES; }
    }
}