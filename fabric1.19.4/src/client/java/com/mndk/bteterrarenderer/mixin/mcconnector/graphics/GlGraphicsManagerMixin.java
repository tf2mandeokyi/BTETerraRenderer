package com.mndk.bteterrarenderer.mixin.mcconnector.graphics;

import com.mndk.bteterrarenderer.core.util.IOUtil;
import com.mndk.bteterrarenderer.mcconnector.graphics.GlGraphicsManager;
import com.mndk.bteterrarenderer.mcconnector.wrapper.DrawContextWrapper;
import com.mndk.bteterrarenderer.mcconnector.wrapper.NativeTextureWrapper;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
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
    private static GlGraphicsManager makeInstance() { return new GlGraphicsManager() {
        public void glTranslate(DrawContextWrapper drawContextWrapper, float x, float y, float z) {
            MatrixStack poseStack = drawContextWrapper.get();
            poseStack.translate(x, y, z);
        }
        public void glPushMatrix(DrawContextWrapper drawContextWrapper) {
            MatrixStack poseStack = drawContextWrapper.get();
            poseStack.push();
        }
        public void glPopMatrix(DrawContextWrapper drawContextWrapper) {
            MatrixStack poseStack = drawContextWrapper.get();
            poseStack.pop();
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
        public void setShaderTexture(NativeTextureWrapper textureObject) {
            RenderSystem.setShaderTexture(0, textureObject.get());
        }

        @SneakyThrows
        public NativeTextureWrapper allocateAndGetTextureObject(BufferedImage image) {
            NativeImage nativeImage = NativeImage.read(IOUtil.imageToInputStream(image));
            NativeImageBackedTexture texture = new NativeImageBackedTexture(nativeImage);
            Identifier id = MinecraftClient.getInstance().getTextureManager()
                    .registerDynamicTexture("bteterrarenderer-texture", texture);
            return new NativeTextureWrapper(id);
        }
        public void deleteTextureObject(NativeTextureWrapper textureObject) {
            MinecraftClient.getInstance().getTextureManager().destroyTexture(textureObject.get());
        }

        protected int[] getAbsoluteScissorDimension(DrawContextWrapper drawContextWrapper,
                                                    int relX, int relY, int relWidth, int relHeight) {
            Window window = MinecraftClient.getInstance().getWindow();
            if(window.getScaledWidth() == 0 || window.getScaledHeight() == 0) { // Division by zero handling
                return new int[] { 0, 0, 0, 0 };
            }
            float scaleFactorX = (float) window.getWidth() / window.getScaledWidth();
            float scaleFactorY = (float) window.getHeight() / window.getScaledHeight();

            MatrixStack poseStack = drawContextWrapper.get();
            Matrix4f matrix = poseStack.peek().getPositionMatrix();
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
