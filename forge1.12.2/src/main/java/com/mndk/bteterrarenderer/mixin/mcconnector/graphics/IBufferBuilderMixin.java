package com.mndk.bteterrarenderer.mixin.mcconnector.graphics;

import com.mndk.bteterrarenderer.mcconnector.graphics.IBufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = IBufferBuilder.class, remap = false)
public class IBufferBuilderMixin {

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    private static IBufferBuilder<?> makeFromTessellator() {
        return bTETerraRenderer$of(Tessellator.getInstance());
    }

    @Unique
    private static IBufferBuilder<?> bTETerraRenderer$of(Tessellator tessellator) { return new IBufferBuilder<Object>() {
        public void beginPTCQuads() {
            tessellator.getBuffer().begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        }
        public void beginPTCTriangles() {
            tessellator.getBuffer().begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_COLOR);
        }
        public void ptc(Object poseStack, float x, float y, float z, float u, float v, float r, float g, float b, float a) {
            tessellator.getBuffer().pos(x, y, z).tex(u, v).color(1f, 1f, 1f, a).endVertex();
        }

        public void beginPCQuads() {
            tessellator.getBuffer().begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        }
        public void pc(Object poseStack, float x, float y, float z, float r, float g, float b, float a) {
            tessellator.getBuffer().pos(x, y, z).color(1f, 1f, 1f, a).endVertex();
        }

        public void beginPTQuads() {
            tessellator.getBuffer().begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        }
        public void pt(Object poseStack, float x, float y, float z, float u, float v) {
            tessellator.getBuffer().pos(x, y, z).tex(u, v).endVertex();
        }

        public void drawAndRender() {
            tessellator.draw();
        }
    };}
}
