package com.mndk.bteterrarenderer.connector.gui;

import com.mndk.bteterrarenderer.connector.graphics.GraphicsQuad;
import com.mndk.bteterrarenderer.connector.minecraft.IResourceLocation;
import com.mndk.bteterrarenderer.connector.minecraft.IResourceLocationImpl;
import com.mndk.bteterrarenderer.gui.components.AbstractGuiScreen;
import com.mndk.bteterrarenderer.gui.components.GuiAbstractWidgetImpl;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;

public class GuiStaticConnectorImpl implements GuiStaticConnector {
    private static final ResourceLocation CHECKBOX = new ResourceLocation("textures/gui/checkbox.png");

    @Override
    public void displayGuiScreen(AbstractGuiScreen gui) {
        Minecraft.getInstance().setScreen(new AbstractGuiScreenImpl(gui));
    }

    @Override
    public void fillQuad(Object poseStack, GraphicsQuad<GraphicsQuad.Pos> quad, int color) {
        GraphicsQuad.Pos v0 = quad.get(0), v1 = quad.get(1), v2 = quad.get(2), v3 = quad.get(3);
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

    @Override
    public void drawCheckBox(Object poseStack, int x, int y, int width, int height, boolean focused, boolean checked) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, CHECKBOX);
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
        this.drawBufferPosTex(bufferbuilder, matrix, x, y, width, height, u1, v1, u2, v2);
    }

    @Override
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

    @Override
    public void drawImage(Object poseStack, IResourceLocation res, int x, int y, int w, int h, float u1, float v1, float u2, float v2) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, ((IResourceLocationImpl) res).delegate());
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();

        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        Matrix4f matrix = ((PoseStack) poseStack).last().pose();
        this.drawBufferPosTex(bufferbuilder, matrix, x, y, w, h, u1, v1, u2, v2);
    }

    @Override
    public void drawButton(Object poseStack, int x, int y, int width, int height, GuiAbstractWidgetImpl.HoverState hoverState) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, AbstractWidget.WIDGETS_LOCATION);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();

        int i = 0;
        switch(hoverState) {
            case DISABLED:          break;
            case DEFAULT:    i = 1; break;
            case MOUSE_OVER: i = 2; break;
        }

        PoseStack realPoseStack = (PoseStack) poseStack;
        GuiComponent.blit(realPoseStack, x, y, 0, 0, 46 + i * 20, width / 2, height, 256, 256);
        GuiComponent.blit(realPoseStack, x + width / 2, y, 0, 200 - width / 2, 46 + i * 20, width / 2, height, 256, 256);
    }

    private void drawBufferPosTex(BufferBuilder bufferBuilder, Matrix4f matrix, int x, int y, int w, int h, float u1, float v1, float u2, float v2) {
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferBuilder.vertex(matrix, x, y+h, 0).uv(u1, v2).endVertex();
        bufferBuilder.vertex(matrix, x+w, y+h, 0).uv(u2, v2).endVertex();
        bufferBuilder.vertex(matrix, x+w, y, 0).uv(u2, v1).endVertex();
        bufferBuilder.vertex(matrix, x, y, 0).uv(u1, v1).endVertex();
        bufferBuilder.end();
        BufferUploader.end(bufferBuilder);
    }
}
