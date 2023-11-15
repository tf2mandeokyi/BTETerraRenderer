package com.mndk.bteterrarenderer.mod.client.mixin.gui;

import lombok.experimental.UtilityClass;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;

@UtilityClass
public class RawGuiManagerImpl {

    public final Identifier CHECKBOX_SELECTED_HIGHLIGHTED = new Identifier("widget/checkbox_selected_highlighted");
    public final Identifier CHECKBOX_SELECTED = new Identifier("widget/checkbox_selected");
    public final Identifier CHECKBOX_HIGHLIGHTED = new Identifier("widget/checkbox_highlighted");
    public final Identifier CHECKBOX = new Identifier("widget/checkbox");
    public final ButtonTextures BUTTON_TEXTURES = new ButtonTextures(
            new Identifier("widget/button"),
            new Identifier("widget/button_disabled"),
            new Identifier("widget/button_highlighted")
    );

    public void drawBufferPosTex(BufferBuilder bufferBuilder,
                                 Matrix4f matrix,
                                 int x, int y, int w, int h,
                                 float u0, float v0, float u1, float v1) {
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        bufferBuilder.vertex(matrix, x, y+h, 0).texture(u0, v1).next();
        bufferBuilder.vertex(matrix, x+w, y+h, 0).texture(u1, v1).next();
        bufferBuilder.vertex(matrix, x+w, y, 0).texture(u1, v0).next();
        bufferBuilder.vertex(matrix, x, y, 0).texture(u0, v0).next();
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
    }
}
