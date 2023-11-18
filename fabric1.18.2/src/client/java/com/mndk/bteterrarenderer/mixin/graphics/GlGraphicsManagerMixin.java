package com.mndk.bteterrarenderer.mixin.graphics;

import com.mndk.bteterrarenderer.core.graphics.GlGraphicsManager;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.experimental.UtilityClass;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@UtilityClass
@Mixin(value = GlGraphicsManager.class, remap = false)
public class GlGraphicsManagerMixin {

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public void glTranslate(Object poseStack, float x, float y, float z) {
        ((MatrixStack) poseStack).translate(x, y, z);
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public void glPushMatrix(Object poseStack) {
        ((MatrixStack) poseStack).push();
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public void glPopMatrix(Object poseStack) {
        ((MatrixStack) poseStack).pop();
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    private int[] getAbsoluteScissorDimension(Object poseStack, int relX, int relY, int relWidth, int relHeight) {
        Window window = MinecraftClient.getInstance().getWindow();
        if(window.getScaledWidth() == 0 || window.getScaledHeight() == 0) { // Division by zero handling
            return new int[] { 0, 0, 0, 0 };
        }
        float scaleFactorX = (float) window.getWidth() / window.getScaledWidth();
        float scaleFactorY = (float) window.getHeight() / window.getScaledHeight();

        Matrix4f matrix = ((MatrixStack) poseStack).peek().getPositionMatrix();
        Vector4f start = new Vector4f(relX, relY, 0, 1);
        Vector4f end = new Vector4f(relX + relWidth, relY + relHeight, 0, 1);
        start.transform(matrix);
        end.transform(matrix);

        int scissorX = (int) (scaleFactorX * Math.min(start.getX(), end.getX()));
        int scissorY = (int) (window.getHeight() - scaleFactorY * Math.max(start.getY(), end.getY()));
        int scissorWidth = (int) (scaleFactorX * Math.abs(start.getX() - end.getX()));
        int scissorHeight = (int) (scaleFactorY * Math.abs(start.getY() - end.getY()));
        return new int[] { scissorX, scissorY, scissorWidth, scissorHeight };
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    private void glEnableScissorTest() {
        RenderSystem.assertOnGameThreadOrInit();
        GlStateManager._enableScissorTest();
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    private void glScissorBox(int x, int y, int width, int height) {
        RenderSystem.assertOnGameThreadOrInit();
        GlStateManager._scissorBox(x, y, width, height);
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    private void glDisableScissorTest() {
        RenderSystem.disableScissor();
    }
}
