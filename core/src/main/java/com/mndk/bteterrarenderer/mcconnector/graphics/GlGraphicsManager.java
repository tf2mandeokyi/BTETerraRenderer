package com.mndk.bteterrarenderer.mcconnector.graphics;

import com.mndk.bteterrarenderer.core.util.BTRUtil;
import com.mndk.bteterrarenderer.mcconnector.MixinUtil;
import com.mndk.bteterrarenderer.mcconnector.client.WindowManager;

import java.awt.image.BufferedImage;
import java.util.Stack;

public abstract class GlGraphicsManager<PoseStack, TextureObject> {

    public static final GlGraphicsManager<Object, Object> INSTANCE = BTRUtil.uncheckedCast(makeInstance());
    private static GlGraphicsManager<?,?> makeInstance() {
        return MixinUtil.notOverwritten();
    }

    public abstract void glTranslate(PoseStack poseStack, float x, float y, float z);
    public abstract void glPushMatrix(PoseStack poseStack);
    public abstract void glPopMatrix(PoseStack poseStack);
    public abstract void glEnableTexture();
    public abstract void glDisableTexture();
    public abstract void glEnableCull();
    public abstract void glDisableCull();
    public abstract void glEnableBlend();
    public abstract void glDisableBlend();
    public abstract void glSetAlphaBlendFunc();
    public abstract void glDefaultBlendFunc();

    public abstract void setPositionTexShader();
    public abstract void setPositionColorShader();
    public abstract void setPositionTexColorShader();
    public abstract void setShaderTexture(TextureObject textureObject);

    public abstract TextureObject allocateAndGetTextureObject(BufferedImage image);
    public abstract void deleteTextureObject(TextureObject textureObject);

    /**
     * Converts "relative" dimension to an absolute scissor dimension
     * @return {@code [ scissorX, scissorY, scissorWidth, scissorHeight ]}
     */
    protected abstract int[] getAbsoluteScissorDimension(PoseStack poseStack, int relX, int relY, int relWidth, int relHeight);
    protected abstract void glEnableScissorTest();
    protected abstract void glScissorBox(int x, int y, int width, int height);
    protected abstract void glDisableScissorTest();

    private final Stack<int[]> SCISSOR_DIM_STACK = new Stack<>();

    public void pushRelativeScissor(PoseStack poseStack, int relX, int relY, int relWidth, int relHeight) {
        int[] scissorDimension = getAbsoluteScissorDimension(poseStack, relX, relY, relWidth, relHeight);
        SCISSOR_DIM_STACK.push(scissorDimension);
        this.updateScissorBox();
    }

    public void popRelativeScissor() {
        if(!SCISSOR_DIM_STACK.isEmpty()) SCISSOR_DIM_STACK.pop();
        this.updateScissorBox();
    }

    private void updateScissorBox() {
        this.glDisableScissorTest();
        if(SCISSOR_DIM_STACK.isEmpty()) return;

        // Calculate intersections
        int totalMinX = 0, totalMaxX = WindowManager.getPixelWidth();
        int totalMinY = 0, totalMaxY = WindowManager.getPixelHeight();
        for(int[] dimension : SCISSOR_DIM_STACK) {
            int minX = dimension[0], maxX = dimension[0] + dimension[2];
            int minY = dimension[1], maxY = dimension[1] + dimension[3];
            if(totalMinX < minX) totalMinX = minX;
            if(totalMinY < minY) totalMinY = minY;
            if(totalMaxX > maxX) totalMaxX = maxX;
            if(totalMaxY > maxY) totalMaxY = maxY;
        }

        // Range validation
        if(totalMinX > totalMaxX) totalMaxX = totalMinX;
        if(totalMinY > totalMaxY) totalMaxY = totalMinY;

        // Do scissor
        int scissorX = totalMinX, scissorWidth = totalMaxX - totalMinX;
        int scissorY = totalMinY, scissorHeight = totalMaxY - totalMinY;
        this.glEnableScissorTest();
        this.glScissorBox(scissorX, scissorY, scissorWidth, scissorHeight);
    }
}
