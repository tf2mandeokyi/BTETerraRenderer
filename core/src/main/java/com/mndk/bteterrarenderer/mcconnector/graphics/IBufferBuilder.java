package com.mndk.bteterrarenderer.mcconnector.graphics;

import com.mndk.bteterrarenderer.core.util.BTRUtil;
import com.mndk.bteterrarenderer.mcconnector.MixinUtil;

public abstract class IBufferBuilder<PoseStack> {

    private static final IBufferBuilder<Object> TESSELLATOR_INSTANCE = BTRUtil.uncheckedCast(makeFromTessellator());
    private static IBufferBuilder<?> makeFromTessellator() {
        return MixinUtil.notOverwritten();
    }
    public static <T> IBufferBuilder<T> getTessellatorInstance() {
        return BTRUtil.uncheckedCast(TESSELLATOR_INSTANCE);
    }

    /** PTC stands for Position-Texture-Color */
    public abstract void beginPTCQuads();
    /** PTC stands for Position-Texture-Color */
    public abstract void beginPTCTriangles();
    /** PTC stands for Position-Texture-Color */
    public abstract void ptc(PoseStack poseStack,
             float x, float y, float z,
             float u, float v,
             float r, float g, float b, float a);

    /** PC stands for Position-Color */
    public abstract void beginPCQuads();
    /** PC stands for Position-Color */
    public abstract void pc(PoseStack poseStack,
                            float x, float y, float z,
                            float r, float g, float b, float a);

    /** PT stands for Position-Texture */
    public abstract void beginPTQuads();
    /** PT stands for Position-Color */
    public abstract void pt(PoseStack poseStack,
                            float x, float y, float z,
                            float u, float v);

    public abstract void drawAndRender();

}
