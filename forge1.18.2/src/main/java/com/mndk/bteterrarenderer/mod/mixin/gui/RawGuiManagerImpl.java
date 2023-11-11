package com.mndk.bteterrarenderer.mod.mixin.gui;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import lombok.experimental.UtilityClass;
import net.minecraft.resources.ResourceLocation;

@UtilityClass
public class RawGuiManagerImpl {
    public final ResourceLocation CHECKBOX = new ResourceLocation("textures/gui/checkbox.png");

    public void drawBufferPosTex(BufferBuilder bufferBuilder,
                                 Matrix4f matrix,
                                 int x, int y, int w, int h,
                                 float u1, float v1, float u2, float v2) {
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferBuilder.vertex(matrix, x, y+h, 0).uv(u1, v2).endVertex();
        bufferBuilder.vertex(matrix, x+w, y+h, 0).uv(u2, v2).endVertex();
        bufferBuilder.vertex(matrix, x+w, y, 0).uv(u2, v1).endVertex();
        bufferBuilder.vertex(matrix, x, y, 0).uv(u1, v1).endVertex();
        bufferBuilder.end();
        BufferUploader.end(bufferBuilder);
    }
}
