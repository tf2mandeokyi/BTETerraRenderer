package com.mndk.bteterrarenderer.core.graphics;

import com.mndk.bteterrarenderer.core.util.mixin.MixinUtil;
import lombok.experimental.UtilityClass;

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
    /* Use polygon intersection instead. this is quite unstable */
    public void glEnableScissorTest() {
        MixinUtil.notOverwritten();
    }
    public void glDisableScissorTest() {
        MixinUtil.notOverwritten();
    }
    public void glRelativeScissor(Object poseStack, int x, int y, int width, int height) {
        MixinUtil.notOverwritten(poseStack, x, y, width, height);
    }
}
