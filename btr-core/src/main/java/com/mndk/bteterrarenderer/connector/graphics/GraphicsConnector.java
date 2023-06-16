package com.mndk.bteterrarenderer.connector.graphics;

import com.mndk.bteterrarenderer.connector.ImplFinder;

public interface GraphicsConnector {
    GraphicsConnector INSTANCE = ImplFinder.search();
    void glTranslate(Object poseStack, float x, float y, float z);
    void glPushMatrix(Object poseStack);
    void glPopMatrix(Object poseStack);
    /* Use polygon intersection instead. this is quite unstable */
    void glEnableScissorTest();
    void glDisableScissorTest();
    void glRelativeScissor(Object poseStack, int x, int y, int width, int height);
}
