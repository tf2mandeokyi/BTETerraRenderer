package com.mndk.bteterrarenderer.mixin.graphics;

import com.mndk.bteterrarenderer.core.graphics.GlGraphicsManager;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector4f;
import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@UtilityClass
@Mixin(value = GlGraphicsManager.class, remap = false)
public class GlGraphicsManagerMixin {

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public void glTranslate(Object poseStack, float x, float y, float z) {
        ((PoseStack) poseStack).translate(x, y, z);
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public void glPushMatrix(Object poseStack) {
        ((PoseStack) poseStack).pushPose();
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public void glPopMatrix(Object poseStack) {
        ((PoseStack) poseStack).popPose();
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    private int[] getAbsoluteScissorDimension(Object poseStack, int relX, int relY, int relWidth, int relHeight) {
        Window window = Minecraft.getInstance().getWindow();
        if(window.getScreenWidth() == 0 || window.getScreenHeight() == 0) { // Division by zero handling
            return new int[] { 0, 0, 0, 0 };
        }
        float scaleFactorX = (float) window.getScreenWidth() / window.getGuiScaledWidth();
        float scaleFactorY = (float) window.getScreenHeight() / window.getGuiScaledHeight();

        Matrix4f matrix = ((PoseStack) poseStack).last().pose();
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
