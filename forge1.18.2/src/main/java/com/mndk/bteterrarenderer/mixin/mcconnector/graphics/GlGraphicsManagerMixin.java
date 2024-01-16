package com.mndk.bteterrarenderer.mixin.mcconnector.graphics;

import com.mndk.bteterrarenderer.core.util.IOUtil;
import com.mndk.bteterrarenderer.mcconnector.graphics.GlGraphicsManager;
import com.mndk.bteterrarenderer.mcconnector.wrapper.DrawContextWrapper;
import com.mndk.bteterrarenderer.mcconnector.wrapper.NativeTextureWrapper;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector4f;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
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
            PoseStack poseStack = drawContextWrapper.get();
            poseStack.translate(x, y, z);
        }
        public void glPushMatrix(DrawContextWrapper drawContextWrapper) {
            PoseStack poseStack = drawContextWrapper.get();
            poseStack.pushPose();
        }
        public void glPopMatrix(DrawContextWrapper drawContextWrapper) {
            PoseStack poseStack = drawContextWrapper.get();
            poseStack.popPose();
        }
        public void glEnableTexture() {
            RenderSystem.enableTexture();
        }
        public void glDisableTexture() {
            RenderSystem.disableTexture();
        }
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
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        }
        public void glDefaultBlendFunc() {
            RenderSystem.defaultBlendFunc();
        }

        public void setPositionTexShader() {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
        }
        public void setPositionColorShader() {
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
        }
        public void setPositionTexColorShader() {
            RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        }
        public void setShaderTexture(NativeTextureWrapper textureObject) {
            RenderSystem.setShaderTexture(0, textureObject.get());
        }

        @SneakyThrows
        public NativeTextureWrapper allocateAndGetTextureObject(BufferedImage image) {
            NativeImage nativeImage = NativeImage.read(IOUtil.imageToInputStream(image));
            DynamicTexture texture = new DynamicTexture(nativeImage);
            ResourceLocation location = Minecraft.getInstance().getTextureManager()
                    .register("bteterrarenderer-textures", texture);
            return new NativeTextureWrapper(location);
        }
        public void deleteTextureObject(NativeTextureWrapper textureObject) {
            Minecraft.getInstance().getTextureManager().release(textureObject.get());
        }

        protected int[] getAbsoluteScissorDimension(DrawContextWrapper drawContextWrapper,
                                                    int relX, int relY, int relWidth, int relHeight) {
            Window window = Minecraft.getInstance().getWindow();
            if(window.getScreenWidth() == 0 || window.getScreenHeight() == 0) { // Division by zero handling
                return new int[] { 0, 0, 0, 0 };
            }
            float scaleFactorX = (float) window.getScreenWidth() / window.getGuiScaledWidth();
            float scaleFactorY = (float) window.getScreenHeight() / window.getGuiScaledHeight();

            PoseStack poseStack = drawContextWrapper.get();
            Matrix4f matrix = poseStack.last().pose();
            Vector4f start = new Vector4f(relX, relY, 0, 1);
            Vector4f end = new Vector4f(relX + relWidth, relY + relHeight, 0, 1);
            start.transform(matrix);
            end.transform(matrix);

            int scissorX = (int) (scaleFactorX * Math.min(start.x(), end.x()));
            int scissorY = (int) (window.getScreenHeight() - scaleFactorY * Math.max(start.y(), end.y()));
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
