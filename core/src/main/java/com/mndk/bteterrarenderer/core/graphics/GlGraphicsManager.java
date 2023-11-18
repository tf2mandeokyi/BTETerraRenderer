package com.mndk.bteterrarenderer.core.graphics;

import com.mndk.bteterrarenderer.core.util.minecraft.WindowManager;
import com.mndk.bteterrarenderer.core.util.mixin.MixinUtil;
import lombok.experimental.UtilityClass;

import java.util.Stack;

@UtilityClass
public class GlGraphicsManager {
    public void glTranslate(Object poseStack, float x, float y, float z) {
        MixinUtil.notOverwritten(poseStack, x, y, z);
    }
    public void glPushMatrix(Object poseStack) {
        MixinUtil.notOverwritten(poseStack);
    }
    public void glPopMatrix(Object poseStack) {
        MixinUtil.notOverwritten(poseStack);
    }

    private final Stack<int[]> SCISSOR_DIM_STACK = new Stack<>();

    public void glPushRelativeScissor(Object poseStack, int relX, int relY, int relWidth, int relHeight) {
        int[] scissorDimension = getAbsoluteScissorDimension(poseStack, relX, relY, relWidth, relHeight);
        SCISSOR_DIM_STACK.push(scissorDimension);
        updateScissorBox();
    }

    public void glPopRelativeScissor() {
        if(!SCISSOR_DIM_STACK.isEmpty()) SCISSOR_DIM_STACK.pop();
        updateScissorBox();
    }

    private void updateScissorBox() {
        glDisableScissorTest();
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
        glEnableScissorTest();
        glScissorBox(scissorX, scissorY, scissorWidth, scissorHeight);
    }

    /**
     * Converts "relative" dimension to an absolute scissor dimension
     * @return {@code [ scissorX, scissorY, scissorWidth, scissorHeight ]}
     */
    private int[] getAbsoluteScissorDimension(Object poseStack, int x, int y, int width, int height) {
        return MixinUtil.notOverwritten(poseStack, x, y, width, height);
    }
    private void glEnableScissorTest() {
        MixinUtil.notOverwritten();
    }
    private void glScissorBox(int x, int y, int width, int height) {
        MixinUtil.notOverwritten(x, y, width, height);
    }
    private void glDisableScissorTest() {
        MixinUtil.notOverwritten();
    }
}
