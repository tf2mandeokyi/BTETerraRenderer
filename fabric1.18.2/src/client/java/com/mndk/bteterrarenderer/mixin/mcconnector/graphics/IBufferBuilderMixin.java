package com.mndk.bteterrarenderer.mixin.mcconnector.graphics;

import com.mndk.bteterrarenderer.mcconnector.graphics.IBufferBuilder;
import com.mndk.bteterrarenderer.mcconnector.wrapper.DrawContextWrapper;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = IBufferBuilder.class, remap = false)
public class IBufferBuilderMixin {

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    private static IBufferBuilder makeFromTessellator() {
        return of(Tessellator.getInstance().getBuffer());
    }

    @Unique
    private static IBufferBuilder of(BufferBuilder builder) { return new IBufferBuilder() {
        public void beginPTCQuads() {
            builder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        }
        public void beginPTCTriangles() {
            builder.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_TEXTURE_COLOR);
        }
        public void ptc(DrawContextWrapper drawContextWrapper, float x, float y, float z, float u, float v, float r, float g, float b, float a) {
            MatrixStack poseStack = drawContextWrapper.get();
            builder.vertex(poseStack.peek().getPositionMatrix(), x, y, z).texture(u, v).color(r, g, b, a).next();
        }

        public void beginPCQuads() {
            builder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        }
        public void pc(DrawContextWrapper drawContextWrapper, float x, float y, float z, float r, float g, float b, float a) {
            MatrixStack poseStack = drawContextWrapper.get();
            builder.vertex(poseStack.peek().getPositionMatrix(), x, y, z).color(r, g, b, a).next();
        }

        public void beginPTQuads() {
            builder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        }
        public void pt(DrawContextWrapper drawContextWrapper, float x, float y, float z, float u, float v) {
            MatrixStack poseStack = drawContextWrapper.get();
            builder.vertex(poseStack.peek().getPositionMatrix(), x, y, z).texture(u, v).next();
        }

        public void drawAndRender() {
            builder.end();
            BufferRenderer.draw(builder);
        }
    };}

}
