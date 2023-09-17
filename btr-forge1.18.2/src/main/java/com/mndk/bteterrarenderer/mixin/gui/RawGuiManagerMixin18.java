package com.mndk.bteterrarenderer.mixin.gui;

import com.mndk.bteterrarenderer.mod.util.mixin.delegate.IResourceLocationImpl18;
import com.mndk.bteterrarenderer.core.graphics.GraphicsQuad;
import com.mndk.bteterrarenderer.core.gui.RawGuiManager;
import com.mndk.bteterrarenderer.core.gui.components.AbstractGuiScreenCopy;
import com.mndk.bteterrarenderer.core.gui.components.GuiAbstractWidgetCopy;
import com.mndk.bteterrarenderer.core.util.mixin.delegate.IResourceLocation;
import com.mndk.bteterrarenderer.mod.util.mixin.graphics.AbstractGuiScreenImpl18;
import com.mndk.bteterrarenderer.mod.util.mixin.gui.RawGuiManagerImpl18;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@UtilityClass
@Mixin(value = RawGuiManager.class, remap = false)
public class RawGuiManagerMixin18 {

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public void displayGuiScreen(AbstractGuiScreenCopy gui) {
        Minecraft.getInstance().setScreen(new AbstractGuiScreenImpl18(gui));
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public void fillQuad(Object poseStack, GraphicsQuad<GraphicsQuad.Pos> quad, int color) {
        GraphicsQuad.Pos v0 = quad.getVertex(0), v1 = quad.getVertex(1), v2 = quad.getVertex(2), v3 = quad.getVertex(3);
        Matrix4f matrix = ((PoseStack) poseStack).last().pose();

        float a = (float)(color >> 24 & 255) / 255.0F;
        float r = (float)(color >> 16 & 255) / 255.0F;
        float g = (float)(color >>  8 & 255) / 255.0F;
        float b = (float)(color       & 255) / 255.0F;

        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferbuilder.vertex(matrix, v0.x, v0.y, v0.z).color(r, g, b, a).endVertex();
        bufferbuilder.vertex(matrix, v1.x, v1.y, v1.z).color(r, g, b, a).endVertex();
        bufferbuilder.vertex(matrix, v2.x, v2.y, v2.z).color(r, g, b, a).endVertex();
        bufferbuilder.vertex(matrix, v3.x, v3.y, v3.z).color(r, g, b, a).endVertex();
        bufferbuilder.end();
        BufferUploader.end(bufferbuilder);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public void drawCheckBox(Object poseStack, int x, int y, int width, int height, boolean focused, boolean checked) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, RawGuiManagerImpl18.CHECKBOX);
        RenderSystem.enableDepthTest();
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();

        Matrix4f matrix = ((PoseStack) poseStack).last().pose();

        float size = 20 / 64f;
        float u1 = focused ? size : 0, v1 = checked ? size : 0;
        float u2 = u1 + size, v2 = v1 + size;
        RawGuiManagerImpl18.drawBufferPosTex(bufferbuilder, matrix, x, y, width, height, u1, v1, u2, v2);
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public void drawTextFieldHighlight(Object poseStack, int startX, int startY, int endX, int endY) {
        RenderSystem.setShader(GameRenderer::getPositionShader);
        RenderSystem.setShaderColor(0.0F, 0.0F, 1.0F, 1.0F);
        RenderSystem.disableTexture();
        RenderSystem.enableColorLogicOp();
        RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();

        Matrix4f matrix = ((PoseStack) poseStack).last().pose();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        bufferbuilder.vertex(matrix, startX, endY, 0).endVertex();
        bufferbuilder.vertex(matrix, endX, endY, 0).endVertex();
        bufferbuilder.vertex(matrix, endX, startY, 0).endVertex();
        bufferbuilder.vertex(matrix, startX, startY, 0).endVertex();
        tesselator.end();

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableColorLogicOp();
        RenderSystem.enableTexture();
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public void drawImage(Object poseStack, IResourceLocation res, int x, int y, int w, int h, float u1, float v1, float u2, float v2) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, ((IResourceLocationImpl18) res).delegate());
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();

        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        Matrix4f matrix = ((PoseStack) poseStack).last().pose();
        RawGuiManagerImpl18.drawBufferPosTex(bufferbuilder, matrix, x, y, w, h, u1, v1, u2, v2);
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public void drawButton(Object poseStack, int x, int y, int width, int height, GuiAbstractWidgetCopy.HoverState hoverState) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, AbstractWidget.WIDGETS_LOCATION);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();

        int i = switch (hoverState) {
            case DISABLED -> 0;
            case DEFAULT -> 1;
            case MOUSE_OVER -> 2;
        };

        PoseStack realPoseStack = (PoseStack) poseStack;
        GuiComponent.blit(realPoseStack, x, y, 0, 0, 46 + i * 20, width / 2, height, 256, 256);
        GuiComponent.blit(realPoseStack, x + width / 2, y, 0, 200 - (float) width / 2, 46 + i * 20, width / 2, height, 256, 256);
    }
}
