package com.mndk.bteterrarenderer.connector.graphics;

import com.mndk.bteterrarenderer.connector.ConnectorImpl;
import com.mndk.bteterrarenderer.connector.minecraft.IResourceLocation;
import com.mndk.bteterrarenderer.connector.minecraft.IResourceLocationImpl;
import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.nio.FloatBuffer;

@ConnectorImpl
@SuppressWarnings("unused")
public class GraphicsConnectorImpl implements GraphicsConnector {

    public static PoseStack POSE_STACK = null;

    public void glPushAttrib() {
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_LIGHTING_BIT);
    }
    public void glPopAttrib() {
        GL11.glPopAttrib();
    }
    public void glTranslate(float x, float y, float z) {
        POSE_STACK.translate(x, y, z);
    }
    public void glColor(float r, float g, float b, float a) {
        GL11.glColor4f(r, g, b, a);
    }
    public void glPushMatrix() {
        POSE_STACK.pushPose();
    }
    public void glPopMatrix() {
        POSE_STACK.popPose();
    }
    public void glEnableScissorTest() {
        GlStateManager._enableScissorTest();
    }
    public void glDisableScissorTest() {
        GlStateManager._disableScissorTest();
    }
    public void glEnableBlend() {
        RenderSystem.enableBlend();
    }
    public void glDisableBlend() {
        RenderSystem.disableBlend();
    }
    public void glEnableTexture2D() {
        GL11.glEnable(GlConst.GL_TEXTURE_2D);
    }
    public void glDisableTexture2D() {
        GL11.glDisable(GlConst.GL_TEXTURE_2D);
    }
    public void glRelativeScissor(int x, int y, int width, int height) {
        Window window = Minecraft.getInstance().getWindow();

        FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
        RenderSystem.getModelViewMatrix().store(buffer);
        buffer.rewind();

        int translateX = (int) buffer.get(12), translateY = (int) buffer.get(13);
        int scaleFactorX = window.getGuiScaledWidth() / window.getScreenWidth();
        int scaleFactorY = window.getGuiScaledHeight() / window.getScreenHeight();
        GL11.glScissor(
                scaleFactorX * (x + translateX), window.getGuiScaledHeight() - scaleFactorY * (y + translateY + height),
                scaleFactorX * width, scaleFactorY * height
        );
    }
    public void glTryBlendFuncSeparate(GlFactor srcFactor, GlFactor dstFactor, GlFactor srcFactorAlpha, GlFactor dstFactorAlpha) {
        RenderSystem.blendFuncSeparate(srcFactor.srcFactor, dstFactor.dstFactor, srcFactorAlpha.srcFactor, dstFactorAlpha.dstFactor);
    }

    public IBufferBuilderImpl getBufferBuilder() {
        return new IBufferBuilderImpl(Tesselator.getInstance().getBuilder());
    }
    public void tessellatorDraw() {
        Tesselator.getInstance().end();
    }
    public void bindTexture(IResourceLocation res) {
        ResourceLocation resourceLocation = ((IResourceLocationImpl) res).delegate();
        Minecraft.getInstance().getTextureManager().bindForSetup(resourceLocation);
    }
}
