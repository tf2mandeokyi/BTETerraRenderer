package com.mndk.bteterrarenderer.mixin.graphics;

import com.mndk.bteterrarenderer.core.graphics.GlGraphicsManager;
import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.nio.FloatBuffer;

@UtilityClass
@Mixin(value = GlGraphicsManager.class, remap = false)
public class GlGraphicsManagerMixin {

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public void glTranslate(Object poseStack, float x, float y, float z) {
        GlStateManager.translate(x, y, z);
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public void glPushMatrix(Object poseStack) {
        GlStateManager.pushMatrix();
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public void glPopMatrix(Object poseStack) {
        GlStateManager.popMatrix();
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public boolean glEnableRelativeScissor(Object poseStack, int x, int y, int width, int height) {
        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution scaledResolution = new ScaledResolution(mc);
        int scaleFactor = scaledResolution.getScaleFactor();

        FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, buffer);
        Matrix4f matrix4f = new Matrix4f();
        matrix4f.load(buffer);

        Vector4f originalStart = new Vector4f(x, y, 0, 1);
        Vector4f originalEnd = new Vector4f(x+width, y+height, 0, 1);
        Vector4f start = Matrix4f.transform(matrix4f, originalStart, null);
        Vector4f end = Matrix4f.transform(matrix4f, originalEnd, null);

        int scissorX = (int) (scaleFactor * Math.min(start.x, end.x));
        int scissorY = (int) (mc.displayHeight - scaleFactor * Math.max(start.y, end.y));
        int scissorWidth = (int) (scaleFactor * Math.abs(start.x - end.x));
        int scissorHeight = (int) (scaleFactor * Math.abs(start.y - end.y));

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        int scissorNorth = scissorY + scissorHeight;
        if(scissorNorth < 0) return false;

        GL11.glScissor(scissorX, scissorY, scissorWidth, scissorHeight);
        return true;
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public void glDisableScissorTest() {
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

}
