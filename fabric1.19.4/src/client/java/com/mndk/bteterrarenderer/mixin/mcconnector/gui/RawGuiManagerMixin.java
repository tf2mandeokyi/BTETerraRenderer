package com.mndk.bteterrarenderer.mixin.mcconnector.gui;

import com.mndk.bteterrarenderer.mcconnector.IResourceLocation;
import com.mndk.bteterrarenderer.mcconnector.gui.RawGuiManager;
import com.mndk.bteterrarenderer.mcconnector.gui.components.AbstractGuiScreenCopy;
import com.mndk.bteterrarenderer.mcconnector.gui.components.GuiAbstractWidgetCopy;
import com.mndk.bteterrarenderer.mod.mcconnector.IResourceLocationIdentifierImpl;
import com.mndk.bteterrarenderer.mod.mcconnector.gui.AbstractGuiScreenImpl;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.experimental.UtilityClass;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

@UtilityClass
@Mixin(value = RawGuiManager.class, remap = false)
public class RawGuiManagerMixin {

    @Unique
    private final Identifier CHECKBOX = new Identifier("textures/gui/checkbox.png");

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    private static RawGuiManager<MatrixStack> makeInstance() { return new RawGuiManager<>() {

        public void displayGuiScreen(AbstractGuiScreenCopy gui) {
            MinecraftClient.getInstance().setScreen(new AbstractGuiScreenImpl(gui));
        }

        public void drawButton(MatrixStack poseStack, int x, int y, int width, int height, GuiAbstractWidgetCopy.HoverState hoverState) {
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

            DrawableHelper.drawTexture(poseStack, x, y, 0, 0, 46 + i * 20, width / 2, height, 256, 256);
            DrawableHelper.drawTexture(poseStack, x + width / 2, y, 0, 200 - (float) width / 2, 46 + i * 20, width / 2, height, 256, 256);
        }

        public void drawCheckBox(MatrixStack poseStack, int x, int y, int width, int height, boolean focused, boolean checked) {
            RenderSystem.setShader(GameRenderer::getPositionTexProgram);
            RenderSystem.setShaderTexture(0, CHECKBOX);
            RenderSystem.enableDepthTest();
            RenderSystem.setShaderColor(1, 1, 1, 1);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuffer();

            Matrix4f matrix = poseStack.peek().getPositionMatrix();

            float size = 20 / 64f;
            float u0 = focused ? size : 0, v0 = checked ? size : 0;
            float u1 = u0 + size, v1 = v0 + size;
            drawBufferPosTex(bufferbuilder, matrix, x, y, width, height, u0, v0, u1, v1);
        }

        public void drawTextFieldHighlight(MatrixStack poseStack, int startX, int startY, int endX, int endY) {
            RenderSystem.setShader(GameRenderer::getPositionProgram);
            RenderSystem.setShaderColor(0.0F, 0.0F, 1.0F, 1.0F);
            RenderSystem.enableColorLogicOp();
            RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE);

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuffer();

            Matrix4f matrix = poseStack.peek().getPositionMatrix();
            bufferbuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
            bufferbuilder.vertex(matrix, startX, endY, 0).next();
            bufferbuilder.vertex(matrix, endX, endY, 0).next();
            bufferbuilder.vertex(matrix, endX, startY, 0).next();
            bufferbuilder.vertex(matrix, startX, startY, 0).next();
            tessellator.draw();

            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.disableColorLogicOp();
        }

        public void drawImage(MatrixStack poseStack, IResourceLocation res, int x, int y, int w, int h, float u1, float v1, float u2, float v2) {
            RenderSystem.setShader(GameRenderer::getPositionTexProgram);
            RenderSystem.setShaderTexture(0, ((IResourceLocationIdentifierImpl) res).delegate());
            RenderSystem.setShaderColor(1, 1, 1, 1);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableDepthTest();

            BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();
            Matrix4f matrix = poseStack.peek().getPositionMatrix();
            drawBufferPosTex(bufferbuilder, matrix, x, y, w, h, u1, v1, u2, v2);
        }

        private void drawBufferPosTex(BufferBuilder bufferBuilder,
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
    };}

}
