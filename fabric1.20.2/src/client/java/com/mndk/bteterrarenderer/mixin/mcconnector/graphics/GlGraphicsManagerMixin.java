package com.mndk.bteterrarenderer.mixin.mcconnector.graphics;

import com.mndk.bteterrarenderer.core.util.IOUtil;
import com.mndk.bteterrarenderer.mcconnector.graphics.GlGraphicsManager;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.Window;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.awt.image.BufferedImage;

@UtilityClass
@Mixin(value = GlGraphicsManager.class, remap = false)
public class GlGraphicsManagerMixin {

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    private static GlGraphicsManager<DrawContext, Identifier> makeInstance() { return new GlGraphicsManager<>() {
        public void glTranslate(DrawContext drawContext, float x, float y, float z) {
            drawContext.getMatrices().translate(x, y, z);
        }
        public void glPushMatrix(DrawContext drawContext) {
            drawContext.getMatrices().push();
        }
        public void glPopMatrix(DrawContext drawContext) {
            drawContext.getMatrices().pop();
        }
        public void glEnableTexture() {}
        public void glDisableTexture() {}
        public void glEnableCull() {
            RenderSystem.enableCull();
        }
        public void glDisableCull() {
            RenderSystem.disableCull();
        }
        public void glEnableBlend() {
            RenderSystem.enableBlend();
        }
        public void glDisableBlend() {
            RenderSystem.disableBlend();
        }
        public void glSetAlphaBlendFunc() {
            RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
        }
        public void glDefaultBlendFunc() {
            RenderSystem.defaultBlendFunc();
        }

        public void setPositionTexShader() {
            RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        }
        public void setPositionColorShader() {
            RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        }
        public void setPositionTexColorShader() {
            RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
        }
        public void setShaderTexture(Identifier textureObject) {
            RenderSystem.setShaderTexture(0, textureObject);
        }

        @SneakyThrows
        public Identifier allocateAndGetTextureObject(BufferedImage image) {
            NativeImage nativeImage = NativeImage.read(IOUtil.imageToInputStream(image));
            NativeImageBackedTexture texture = new NativeImageBackedTexture(nativeImage);
            return MinecraftClient.getInstance().getTextureManager().registerDynamicTexture("bteterrarenderer-textures", texture);
        }
        public void deleteTextureObject(Identifier textureObject) {
            MinecraftClient.getInstance().getTextureManager().destroyTexture(textureObject);
        }

        protected int[] getAbsoluteScissorDimension(DrawContext drawContext, int relX, int relY, int relWidth, int relHeight) {
            Window window = MinecraftClient.getInstance().getWindow();
            if(window.getWidth() == 0 || window.getHeight() == 0) { // Division by zero handling
                return new int[] { 0, 0, 0, 0 };
            }
            float scaleFactorX = (float) window.getWidth() / window.getScaledWidth();
            float scaleFactorY = (float) window.getHeight() / window.getScaledHeight();

            Matrix4f matrix = drawContext.getMatrices().peek().getPositionMatrix();
            Vector4f start = new Vector4f(relX, relY, 0, 1);
            Vector4f end = new Vector4f(relX + relWidth, relY + relHeight, 0, 1);
            start = matrix.transform(start);
            end = matrix.transform(end);

            int scissorX = (int) (scaleFactorX * Math.min(start.x(), end.x()));
            int scissorY = (int) (window.getHeight() - scaleFactorY * Math.max(start.y(), end.y()));
            int scissorWidth = (int) (scaleFactorX * Math.abs(start.x() - end.x()));
            int scissorHeight = (int) (scaleFactorY * Math.abs(start.y() - end.y()));
            return new int[] { scissorX, scissorY, scissorWidth, scissorHeight };
        }
        protected void glEnableScissorTest() {
            RenderSystem.assertOnGameThreadOrInit();
            GlStateManager._enableScissorTest();
        }
        protected void glScissorBox(int x, int y, int width, int height) {
            RenderSystem.assertOnGameThreadOrInit();
            GlStateManager._scissorBox(x, y, width, height);
        }
        protected void glDisableScissorTest() {
            RenderSystem.disableScissor();
        }
    };}
}
