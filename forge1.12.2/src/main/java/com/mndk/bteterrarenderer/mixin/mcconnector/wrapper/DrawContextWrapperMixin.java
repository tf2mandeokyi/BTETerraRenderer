package com.mndk.bteterrarenderer.mixin.mcconnector.wrapper;

import com.mndk.bteterrarenderer.mcconnector.graphics.format.PosXY;
import com.mndk.bteterrarenderer.mcconnector.graphics.shape.GraphicsQuad;
import com.mndk.bteterrarenderer.mcconnector.gui.component.GuiEventListenerCopy;
import com.mndk.bteterrarenderer.mcconnector.gui.widget.AbstractWidgetCopy;
import com.mndk.bteterrarenderer.mcconnector.wrapper.*;
import com.mndk.bteterrarenderer.mod.mcconnector.gui.AbstractGuiScreenImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.client.config.GuiUtils;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

import javax.annotation.Nonnull;
import java.nio.FloatBuffer;

@Mixin(value = DrawContextWrapper.class, remap = false)
public class DrawContextWrapperMixin {

    @Unique
    private static final ResourceLocation WIDGET_TEXTURES = new ResourceLocation("textures/gui/widgets.png");

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public static DrawContextWrapper<?> of(@Nonnull Object delegate) { return new DrawContextWrapper<Object>(delegate) {

        public void translate(float x, float y, float z) {
            GlStateManager.translate(x, y, z);
        }
        public void pushMatrix() {
            GlStateManager.pushMatrix();
        }
        public void popMatrix() {
            GlStateManager.popMatrix();
        }

        public int[] getAbsoluteScissorDimension(int relX, int relY, int relWidth, int relHeight) {
            Minecraft mc = Minecraft.getMinecraft();
            ScaledResolution scaledResolution = new ScaledResolution(mc);
            int scaleFactor = scaledResolution.getScaleFactor();

            FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
            GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, buffer);
            Matrix4f matrix4f = new Matrix4f();
            matrix4f.load(buffer);

            Vector4f originalStart = new Vector4f(relX, relY, 0, 1);
            Vector4f originalEnd = new Vector4f(relX+relWidth, relY+relHeight, 0, 1);
            Vector4f start = Matrix4f.transform(matrix4f, originalStart, null);
            Vector4f end = Matrix4f.transform(matrix4f, originalEnd, null);

            int scissorX = (int) (scaleFactor * Math.min(start.x, end.x));
            int scissorY = (int) (mc.displayHeight - scaleFactor * Math.max(start.y, end.y));
            int scissorWidth = (int) (scaleFactor * Math.abs(start.x - end.x));
            int scissorHeight = (int) (scaleFactor * Math.abs(start.y - end.y));
            return new int[] { scissorX, scissorY, scissorWidth, scissorHeight };
        }

        public void fillQuad(GraphicsQuad<PosXY> quad, int color, float z) {
            PosXY v0 = quad.getVertex(0);
            PosXY v1 = quad.getVertex(1);
            PosXY v2 = quad.getVertex(2);
            PosXY v3 = quad.getVertex(3);

            float a = (float)(color >> 24 & 255) / 255.0F;
            float r = (float)(color >> 16 & 255) / 255.0F;
            float g = (float)(color >>  8 & 255) / 255.0F;
            float b = (float)(color       & 255) / 255.0F;

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferBuilder = tessellator.getBuffer();
            GlStateManager.enableBlend();
            GlStateManager.disableTexture2D();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.color(r, g, b, a);

            bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
            bufferBuilder.pos(v0.x, v0.y, z).endVertex();
            bufferBuilder.pos(v1.x, v1.y, z).endVertex();
            bufferBuilder.pos(v2.x, v2.y, z).endVertex();
            bufferBuilder.pos(v3.x, v3.y, z).endVertex();
            tessellator.draw();

            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
        }

        public void drawButton(int x, int y, int width, int height, AbstractWidgetCopy.HoverState hoverState) {
            int i = 0;
            switch(hoverState) {
                case DISABLED:          break;
                case DEFAULT:    i = 1; break;
                case MOUSE_OVER: i = 2; break;
            }
            GuiUtils.drawContinuousTexturedBox(
                    WIDGET_TEXTURES,
                    x, y, 0, 46 + i * 20,
                    width, height, 200, 20,
                    2, 3, 2, 2,
                    0
            );
        }

        public void drawCheckBox(int x, int y, int width, int height, boolean focused, boolean checked) {
            drawButton(x, y, width, height, AbstractWidgetCopy.HoverState.DISABLED);
            if (checked) {
                this.drawCenteredTextWithShadow(FontWrapper.DEFAULT, "x", x + width / 2f + 1, y + 1,
                        GuiEventListenerCopy.NORMAL_TEXT_COLOR);
            }
        }

        public void drawTextHighlight(int startX, int startY, int endX, int endY) {
            GlStateManager.color(0.0F, 0.0F, 255.0F, 255.0F);
            GlStateManager.disableTexture2D();
            GlStateManager.enableColorLogic();
            GlStateManager.colorLogicOp(GlStateManager.LogicOp.OR_REVERSE);

            this.drawPosQuad(startX, startY, endX - startX, endY - startY);

            GlStateManager.disableColorLogic();
            GlStateManager.enableTexture2D();
        }

        public void drawImage(ResourceLocationWrapper<?> res, int x, int y, int w, int h, float u0, float v0, float u1, float v1) {
            ResourceLocation resourceLocation = res.get();
            Minecraft.getMinecraft().renderEngine.bindTexture(resourceLocation);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
            GlStateManager.color(1, 1, 1, 1);
            this.drawPosTexQuad(x, y, w, h, u0, v0, u1, v1);
        }

        public void drawHoverEvent(StyleWrapper styleWrapper, int x, int y) {
            GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;
            if(currentScreen == null) return;
            if(!(currentScreen instanceof AbstractGuiScreenImpl)) return;

            ((AbstractGuiScreenImpl) currentScreen).handleStyleHover(styleWrapper.get(), x, y);
        }

        public void drawNativeImage(NativeTextureWrapper allocatedTextureObject, int x, int y, int w, int h) {
            GlStateManager.color(1, 1, 1, 1);
            super.drawNativeImage(allocatedTextureObject, x, y, w, h);
        }

        public int drawTextWithShadow(FontWrapper<?> fontWrapper, String string, float x, float y, int color) {
            FontRenderer fontRenderer = fontWrapper.get();
            return fontRenderer.drawStringWithShadow(string, x, y, color);
        }

        public int drawTextWithShadow(FontWrapper<?> fontWrapper, TextWrapper textWrapper, float x, float y, int color) {
            FontRenderer fontRenderer = fontWrapper.get();
            ITextComponent textComponent = textWrapper.get();
            String formatted = textComponent.getFormattedText();
            return fontRenderer.drawStringWithShadow(formatted, x, y, color);
        }
    };}

}
