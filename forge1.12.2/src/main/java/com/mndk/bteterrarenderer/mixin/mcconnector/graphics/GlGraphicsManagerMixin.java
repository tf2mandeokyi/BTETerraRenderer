package com.mndk.bteterrarenderer.mixin.mcconnector.graphics;

import com.mndk.bteterrarenderer.mcconnector.graphics.GlGraphicsManager;
import com.mndk.bteterrarenderer.mcconnector.wrapper.NativeTextureWrapper;
import lombok.experimental.UtilityClass;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import org.lwjgl.opengl.GL11;
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
        public void setShaderTexture(NativeTextureWrapper textureObject) {
            GlStateManager.bindTexture(textureObject.get());
        }

        public NativeTextureWrapper allocateAndGetTextureObject(BufferedImage image) {
            int glId = GL11.glGenTextures();
            int width = image.getWidth(), height = image.getHeight();
            TextureUtil.allocateTexture(glId, width, height);

            int[] imageData = new int[width * height];
            image.getRGB(0, 0, width, height, imageData, 0, width);
            TextureUtil.uploadTexture(glId, imageData, width, height);
            return new NativeTextureWrapper(glId);
        }
        public void deleteTextureObject(NativeTextureWrapper textureObject) {
            GlStateManager.deleteTexture(textureObject.get());
        }

        public void glEnableScissorTest() {
            GL11.glEnable(GL11.GL_SCISSOR_TEST);
        }
        public void glScissorBox(int x, int y, int width, int height) {
            GL11.glScissor(x, y, width, height);
        }
        public void glDisableScissorTest() {
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
        }
    };}

}
