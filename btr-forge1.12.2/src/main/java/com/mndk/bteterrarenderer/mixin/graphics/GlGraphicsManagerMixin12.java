package com.mndk.bteterrarenderer.mixin.graphics;

import com.mndk.bteterrarenderer.core.graphics.GlGraphicsManager;
import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.nio.FloatBuffer;

@UtilityClass
@Mixin(value = GlGraphicsManager.class, remap = false)
public class GlGraphicsManagerMixin12 {

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
    public void glEnableScissorTest() {
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public void glDisableScissorTest() {
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
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
