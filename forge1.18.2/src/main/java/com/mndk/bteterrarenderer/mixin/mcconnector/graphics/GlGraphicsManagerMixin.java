package com.mndk.bteterrarenderer.mixin.mcconnector.graphics;

import com.mndk.bteterrarenderer.core.util.IOUtil;
import com.mndk.bteterrarenderer.mcconnector.graphics.GlGraphicsManager;
import com.mndk.bteterrarenderer.mcconnector.wrapper.NativeTextureWrapper;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
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

        public void glEnableScissorTest() {
            RenderSystem.assertOnGameThreadOrInit();
            GlStateManager._enableScissorTest();
        }
        public void glScissorBox(int x, int y, int width, int height) {
            RenderSystem.assertOnGameThreadOrInit();
            GlStateManager._scissorBox(x, y, width, height);
        }
        public void glDisableScissorTest() {
            RenderSystem.disableScissor();
        }
    };}

}
