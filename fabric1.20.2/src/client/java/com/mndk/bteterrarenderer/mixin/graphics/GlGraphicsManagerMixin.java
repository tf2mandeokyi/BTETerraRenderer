package com.mndk.bteterrarenderer.mixin.graphics;

import com.mndk.bteterrarenderer.core.graphics.GlGraphicsManager;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.experimental.UtilityClass;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.Window;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@UtilityClass
@Mixin(value = GlGraphicsManager.class, remap = false)
public class GlGraphicsManagerMixin {

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public void glTranslate(Object drawContext, float x, float y, float z) {
        ((DrawContext) drawContext).getMatrices().translate(x, y, z);
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public void glPushMatrix(Object drawContext) {
        ((DrawContext) drawContext).getMatrices().push();
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public void glPopMatrix(Object drawContext) {
        ((DrawContext) drawContext).getMatrices().pop();
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public boolean glEnableRelativeScissor(Object drawContext, int x, int y, int width, int height) {
        Window window = MinecraftClient.getInstance().getWindow();
        if(window.getWidth() == 0 || window.getHeight() == 0) return false; // Division by zero handling
        float scaleFactorX = (float) window.getWidth() / window.getScaledWidth();
        float scaleFactorY = (float) window.getHeight() / window.getScaledHeight();

        Matrix4f matrix = ((DrawContext) drawContext).getMatrices().peek().getPositionMatrix();
        Vector4f start = new Vector4f(x, y, 0, 1);
        Vector4f end = new Vector4f(x+width, y+height, 0, 1);
        start = matrix.transform(start);
        end = matrix.transform(end);

        int scissorX = (int) (scaleFactorX * Math.min(start.x(), end.x()));
        int scissorY = (int) (window.getHeight() - scaleFactorY * Math.max(start.y(), end.y()));
        int scissorWidth = (int) (scaleFactorX * Math.abs(start.x() - end.x()));
        int scissorHeight = (int) (scaleFactorY * Math.abs(start.y() - end.y()));

        int scissorNorth = scissorY + scissorHeight;
        if(scissorNorth < 0) return false;

        RenderSystem.enableScissor(scissorX, scissorY, scissorWidth, scissorHeight);
        return true;
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public void glDisableScissorTest() {
        RenderSystem.disableScissor();
    }
}
