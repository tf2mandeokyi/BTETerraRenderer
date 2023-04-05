package com.mndk.bteterrarenderer.connector.graphics;

import com.mndk.bteterrarenderer.connector.ConnectorImpl;
import com.mndk.bteterrarenderer.connector.minecraft.IResourceLocation;
import com.mndk.bteterrarenderer.connector.minecraft.IResourceLocationImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;

import java.nio.FloatBuffer;

@ConnectorImpl
public class GraphicsConnectorImpl implements GraphicsConnector {
    public int glGenTextures() {
        return GL11.glGenTextures();
    }
    public void allocateTexture(int glId, int width, int height) {
        TextureUtil.allocateTexture(glId, width, height);
    }
    public void uploadTexture(int glId, int[] imageData, int width, int height) {
        TextureUtil.uploadTexture(glId, imageData, width, height);
    }
    public void glBindTexture(int glId) {
        GlStateManager.bindTexture(glId);
    }
    public void glDeleteTexture(int glId) {
        GlStateManager.deleteTexture(glId);
    }
    public void glPushAttrib() {
        GlStateManager.pushAttrib();
    }
    public void glPopAttrib() {
        GlStateManager.popAttrib();
    }
    public void glTranslate(float x, float y, float z) {
        GlStateManager.translate(x, y, z);
    }
    public void glScale(float x, float y, float z) {
        GlStateManager.scale(x, y, z);
    }
    public void glColor(float r, float g, float b, float a) {
        GlStateManager.color(r, g, b, a);
    }
    public void glPushMatrix() {
        GlStateManager.pushMatrix();
    }
    public void glPopMatrix() {
        GlStateManager.popMatrix();
    }
    public void glEnableScissorTest() {
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
    }
    public void glDisableScissorTest() {
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
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
    public void glEnableTexture2D() {
        GlStateManager.enableTexture2D();
    }
    public void glDisableTexture2D() {
        GlStateManager.disableTexture2D();
    }
    public void glRelativeScissor(int x, int y, int width, int height) {
        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution scaledResolution = new ScaledResolution(mc);

        FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, buffer);
        buffer.rewind();
        Matrix4f matrix4f = new Matrix4f();
        matrix4f.load(buffer);

        int translateX = (int) matrix4f.m30, translateY = (int) matrix4f.m31;
        int scaleFactor = scaledResolution.getScaleFactor();
        GL11.glScissor(
                scaleFactor * (x + translateX), mc.displayHeight - scaleFactor * (y + translateY + height),
                scaleFactor * width, scaleFactor * height
        );
    }
    public void glBlendFunc(GlFactor srcFactor, GlFactor dstFactor) {
        GlStateManager.blendFunc(srcFactor.srcFactor, dstFactor.dstFactor);
    }
    public void glTryBlendFuncSeparate(GlFactor srcFactor, GlFactor dstFactor,
                                       GlFactor srcFactorAlpha, GlFactor dstFactorAlpha) {
        GlStateManager.tryBlendFuncSeparate(
                srcFactor.srcFactor, dstFactor.dstFactor, srcFactorAlpha.srcFactor, dstFactorAlpha.dstFactor);
    }

    public IBufferBuilderImpl getBufferBuilder() {
        return new IBufferBuilderImpl(Tessellator.getInstance().getBuffer());
    }
    public void tessellatorDraw() {
        Tessellator.getInstance().draw();
    }
    public void bindTexture(IResourceLocation res) {
        ResourceLocation resourceLocation = ((IResourceLocationImpl) res).getResourceLocation();
        Minecraft.getMinecraft().renderEngine.bindTexture(resourceLocation);
    }
}
