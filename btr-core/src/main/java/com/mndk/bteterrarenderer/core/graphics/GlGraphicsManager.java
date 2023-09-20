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
    /**
     * @return {@code true} if the scissoring was complete, {@code false} otherwise.
     * */
    public boolean glEnableRelativeScissor(Object poseStack, int x, int y, int width, int height) {
        return MixinUtil.notOverwritten(poseStack, x, y, width, height);
    }
    public void glDisableScissorTest() {
        MixinUtil.notOverwritten();
    }
}
