package com.mndk.bteterrarenderer.connector;

import com.mndk.bteterrarenderer.connector.gui.IGuiChat;
import com.mndk.bteterrarenderer.connector.gui.IGuiTextField;
import com.mndk.bteterrarenderer.connector.minecraft.IResourceLocation;
import com.mndk.bteterrarenderer.connector.minecraft.IResourceLocationImpl;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

@ConnectorImpl
@SuppressWarnings("unused")
public class DependencyConnectorSupplierImpl implements DependencyConnectorSupplier {

    private static final IResourceLocation WIDGET_TEXTURES = new IResourceLocationImpl(new DummyGuiButton().getWidgetTextures());

    public IGuiChat newGuiChat() {
//        return new IGuiChatImpl(); // TODO implement this
        return null;
    }

    public IGuiTextField newGuiTextField(int componentId, int x, int y, int width, int height) {

//        return new IGuiTextFieldImpl(componentId, x, y, width, height); // TODO implement this
        return null;
    }

    public IResourceLocation newResourceLocation(String modId, String location) {
        return new IResourceLocationImpl(new ResourceLocation(modId, location));
    }

    @Override
    public IResourceLocation getWidgetTextures() {
        return WIDGET_TEXTURES;
    }

    private static class DummyGuiButton extends AbstractWidget {
        public DummyGuiButton() { super(0, 0, 0, 0, new TextComponent("")); }
        public ResourceLocation getWidgetTextures() { return WIDGETS_LOCATION; }
        public void updateNarration(@NotNull NarrationElementOutput p_169152_) {}
    }
}