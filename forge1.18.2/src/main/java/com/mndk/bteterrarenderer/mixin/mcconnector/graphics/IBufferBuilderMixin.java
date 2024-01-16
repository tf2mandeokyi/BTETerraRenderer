package com.mndk.bteterrarenderer.mixin.mcconnector.graphics;

import com.mndk.bteterrarenderer.mcconnector.graphics.IBufferBuilder;
import com.mndk.bteterrarenderer.mcconnector.wrapper.DrawContextWrapper;
import com.mojang.blaze3d.vertex.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = IBufferBuilder.class, remap = false)
public class IBufferBuilderMixin {

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    private static IBufferBuilder makeFromTessellator() {
        return bTETerraRenderer$of(Tesselator.getInstance().getBuilder());
    }

    @Unique
    private static IBufferBuilder bTETerraRenderer$of(BufferBuilder builder) { return new IBufferBuilder() {
        public void beginPTCQuads() {
            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        }
        public void beginPTCTriangles() {
            builder.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_TEX_COLOR);
        }
        public void ptc(DrawContextWrapper drawContextWrapper, float x, float y, float z, float u, float v, float r, float g, float b, float a) {
            PoseStack poseStack = drawContextWrapper.get();
            builder.vertex(poseStack.last().pose(), x, y, z).uv(u, v).color(r, g, b, a).endVertex();
        }

        public void beginPCQuads() {
            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        }
        public void pc(DrawContextWrapper drawContextWrapper, float x, float y, float z, float r, float g, float b, float a) {
            PoseStack poseStack = drawContextWrapper.get();
            builder.vertex(poseStack.last().pose(), x, y, z).color(r, g, b, a).endVertex();
        }

        public void beginPTQuads() {
            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        }
        public void pt(DrawContextWrapper drawContextWrapper, float x, float y, float z, float u, float v) {
            PoseStack poseStack = drawContextWrapper.get();
            builder.vertex(poseStack.last().pose(), x, y, z).uv(u, v).endVertex();
        }

        public void drawAndRender() {
            builder.end();
            BufferUploader.end(builder);
        }
    };}

}
