package com.mndk.bteterrarenderer.connector.graphics;

import com.mndk.bteterrarenderer.connector.ConnectorImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;

import java.nio.FloatBuffer;

@ConnectorImpl
@SuppressWarnings("unused")
public class GraphicsConnectorImpl implements GraphicsConnector {

    public void glTranslate(Object poseStack, float x, float y, float z) {
        GlStateManager.translate(x, y, z);
    }
    public void glPushMatrix(Object poseStack) {
        GlStateManager.pushMatrix();
    }
    public void glPopMatrix(Object poseStack) {
        GlStateManager.popMatrix();
    }
    public void glEnableScissorTest() {
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
    }
    public void glDisableScissorTest() {
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    public void glRelativeScissor(Object poseStack, int x, int y, int width, int height) {
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

}
