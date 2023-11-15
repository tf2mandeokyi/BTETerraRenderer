package com.mndk.bteterrarenderer.mixin.gui;

import com.mndk.bteterrarenderer.core.graphics.GraphicsQuad;
import com.mndk.bteterrarenderer.core.gui.RawGuiManager;
import com.mndk.bteterrarenderer.core.gui.components.AbstractGuiScreenCopy;
import com.mndk.bteterrarenderer.core.gui.components.GuiAbstractWidgetCopy;
import com.mndk.bteterrarenderer.core.util.mixin.delegate.IResourceLocation;
import com.mndk.bteterrarenderer.mod.client.mixin.delegate.IResourceLocationIdentifierImpl;
import com.mndk.bteterrarenderer.mod.client.mixin.graphics.AbstractGuiScreenImpl;
import com.mndk.bteterrarenderer.mod.client.mixin.gui.RawGuiManagerImpl;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.experimental.UtilityClass;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@UtilityClass
@Mixin(value = RawGuiManager.class, remap = false)
public class RawGuiManagerMixin {

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public void displayGuiScreen(AbstractGuiScreenCopy gui) {
        MinecraftClient.getInstance().setScreen(new AbstractGuiScreenImpl(gui));
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public void fillQuad(Object poseStack, GraphicsQuad<GraphicsQuad.PosXY> quad, int color, float z) {
        GraphicsQuad.PosXY v0 = quad.getVertex(0), v1 = quad.getVertex(1), v2 = quad.getVertex(2), v3 = quad.getVertex(3);
        Matrix4f matrix = ((MatrixStack) poseStack).peek().getPositionMatrix();

        float a = (float)(color >> 24 & 255) / 255.0F;
        float r = (float)(color >> 16 & 255) / 255.0F;
        float g = (float)(color >>  8 & 255) / 255.0F;
        float b = (float)(color       & 255) / 255.0F;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        bufferbuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        bufferbuilder.vertex(matrix, v0.x, v0.y, z).color(r, g, b, a).next();
        bufferbuilder.vertex(matrix, v1.x, v1.y, z).color(r, g, b, a).next();
        bufferbuilder.vertex(matrix, v2.x, v2.y, z).color(r, g, b, a).next();
        bufferbuilder.vertex(matrix, v3.x, v3.y, z).color(r, g, b, a).next();
        BufferRenderer.drawWithGlobalProgram(bufferbuilder.end());
        RenderSystem.disableBlend();
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public void drawCheckBox(Object poseStack, int x, int y, int width, int height, boolean focused, boolean checked) {
        RenderSystem.setShaderTexture(0, RawGuiManagerImpl.CHECKBOX);
        RenderSystem.enableDepthTest();
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();

        Matrix4f matrix = ((MatrixStack) poseStack).peek().getPositionMatrix();

        float size = 20 / 64f;
        float u0 = focused ? size : 0, v0 = checked ? size : 0;
        float u1 = u0 + size, v1 = v0 + size;
        RawGuiManagerImpl.drawBufferPosTex(bufferbuilder, matrix, x, y, width, height, u0, v0, u1, v1);
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public void drawTextFieldHighlight(Object poseStack, int startX, int startY, int endX, int endY) {
        RenderSystem.setShader(GameRenderer::getPositionProgram);
        RenderSystem.setShaderColor(0.0F, 0.0F, 1.0F, 1.0F);
        RenderSystem.enableColorLogicOp();
        RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();

        Matrix4f matrix = ((MatrixStack) poseStack).peek().getPositionMatrix();
        bufferbuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
        bufferbuilder.vertex(matrix, startX, endY, 0).next();
        bufferbuilder.vertex(matrix, endX, endY, 0).next();
        bufferbuilder.vertex(matrix, endX, startY, 0).next();
        bufferbuilder.vertex(matrix, startX, startY, 0).next();
        tessellator.draw();

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableColorLogicOp();

    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public void drawImage(Object poseStack, IResourceLocation res, int x, int y, int w, int h, float u1, float v1, float u2, float v2) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderTexture(0, ((IResourceLocationIdentifierImpl) res).delegate());
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();

        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();
        Matrix4f matrix = ((MatrixStack) poseStack).peek().getPositionMatrix();
        RawGuiManagerImpl.drawBufferPosTex(bufferbuilder, matrix, x, y, w, h, u1, v1, u2, v2);
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public void drawButton(Object poseStack, int x, int y, int width, int height, GuiAbstractWidgetCopy.HoverState hoverState) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderTexture(0, ClickableWidget.WIDGETS_TEXTURE);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();

        int i = switch (hoverState) {
            case DISABLED -> 0;
            case DEFAULT -> 1;
            case MOUSE_OVER -> 2;
        };

        MatrixStack realPoseStack = (MatrixStack) poseStack;
        DrawableHelper.drawTexture(realPoseStack, x, y, 0, 0, 46 + i * 20, width / 2, height, 256, 256);
        DrawableHelper.drawTexture(realPoseStack, x + width / 2, y, 0, 200 - (float) width / 2, 46 + i * 20, width / 2, height, 256, 256);
    }
}
