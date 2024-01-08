package com.mndk.bteterrarenderer.mixin.mcconnector.graphics;

import com.mndk.bteterrarenderer.mcconnector.graphics.GlGraphicsManager;
import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.awt.image.BufferedImage;
import java.nio.FloatBuffer;

@UtilityClass
@Mixin(value = GlGraphicsManager.class, remap = false)
public class GlGraphicsManagerMixin {

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    private static GlGraphicsManager<?,?> makeInstance() { return new GlGraphicsManager<Void, Integer>() {
        public void glTranslate(Void poseStack, float x, float y, float z) {
            GlStateManager.translate(x, y, z);
        }
        public void glPushMatrix(Void poseStack) {
            GlStateManager.pushMatrix();
        }
        public void glPopMatrix(Void poseStack) {
            GlStateManager.popMatrix();
        }
        public void glEnableTexture() {
            GlStateManager.enableTexture2D();
        }
        public void glDisableTexture() {
            GlStateManager.disableTexture2D();
        }
        public void glEnableCull() {
            GlStateManager.enableCull();
        }
        public void glDisableCull() {
            GlStateManager.disableCull();
        }
        public void glEnableBlend() {
            GlStateManager.enableBlend();
        }
        public void glDisableBlend() {
            GlStateManager.disableBlend();
        }
        public void glSetAlphaBlendFunc() {
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        }
        public void glDefaultBlendFunc() {
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        }

        public void setPositionTexShader() {}
        public void setPositionColorShader() {}
        public void setPositionTexColorShader() {}
        public void setShaderTexture(Integer textureObject) {
            GlStateManager.bindTexture(textureObject);
        }

        public Integer allocateAndGetTextureObject(BufferedImage image) {
            int glId = GL11.glGenTextures();
            int width = image.getWidth(), height = image.getHeight();
            TextureUtil.allocateTexture(glId, width, height);

            int[] imageData = new int[width * height];
            image.getRGB(0, 0, width, height, imageData, 0, width);
            TextureUtil.uploadTexture(glId, imageData, width, height);
            return glId;
        }
        public void deleteTextureObject(Integer textureObject) {
            GlStateManager.deleteTexture(textureObject);
        }

        protected int[] getAbsoluteScissorDimension(Void poseStack, int relX, int relY, int relWidth, int relHeight) {
            Minecraft mc = Minecraft.getMinecraft();
            ScaledResolution scaledResolution = new ScaledResolution(mc);
            int scaleFactor = scaledResolution.getScaleFactor();

            FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
            GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, buffer);
            Matrix4f matrix4f = new Matrix4f();
            matrix4f.load(buffer);

            Vector4f originalStart = new Vector4f(relX, relY, 0, 1);
            Vector4f originalEnd = new Vector4f(relX+relWidth, relY+relHeight, 0, 1);
            Vector4f start = Matrix4f.transform(matrix4f, originalStart, null);
            Vector4f end = Matrix4f.transform(matrix4f, originalEnd, null);

            int scissorX = (int) (scaleFactor * Math.min(start.x, end.x));
            int scissorY = (int) (mc.displayHeight - scaleFactor * Math.max(start.y, end.y));
            int scissorWidth = (int) (scaleFactor * Math.abs(start.x - end.x));
            int scissorHeight = (int) (scaleFactor * Math.abs(start.y - end.y));
            return new int[] { scissorX, scissorY, scissorWidth, scissorHeight };
        }
        protected void glEnableScissorTest() {
            GL11.glEnable(GL11.GL_SCISSOR_TEST);
        }
        protected void glScissorBox(int x, int y, int width, int height) {
            GL11.glScissor(x, y, width, height);
        }
        protected void glDisableScissorTest() {
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
        }
    };}

}
