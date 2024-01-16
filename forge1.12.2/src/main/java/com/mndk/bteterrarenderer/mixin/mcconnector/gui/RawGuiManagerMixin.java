package com.mndk.bteterrarenderer.mixin.mcconnector.gui;

import com.mndk.bteterrarenderer.mcconnector.graphics.format.PosXY;
import com.mndk.bteterrarenderer.mcconnector.graphics.shape.GraphicsQuad;
import com.mndk.bteterrarenderer.mcconnector.gui.FontRenderer;
import com.mndk.bteterrarenderer.mcconnector.gui.RawGuiManager;
import com.mndk.bteterrarenderer.mcconnector.gui.component.AbstractGuiScreenCopy;
import com.mndk.bteterrarenderer.mcconnector.gui.component.AbstractWidgetCopy;
import com.mndk.bteterrarenderer.mcconnector.gui.component.GuiEventListenerCopy;
import com.mndk.bteterrarenderer.mcconnector.wrapper.DrawContextWrapper;
import com.mndk.bteterrarenderer.mcconnector.wrapper.ResourceLocationWrapper;
import com.mndk.bteterrarenderer.mod.mcconnector.gui.AbstractGuiScreenImpl;
import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.config.GuiUtils;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

@UtilityClass
@Mixin(value = RawGuiManager.class, remap = false)
public class RawGuiManagerMixin {

    @Unique
    private static final ResourceLocation WIDGET_TEXTURES = new ResourceLocation("textures/gui/widgets.png");

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    private static RawGuiManager makeInstance() { return new RawGuiManager() {
        public void displayGuiScreen(AbstractGuiScreenCopy gui) {
            Minecraft.getMinecraft().displayGuiScreen(new AbstractGuiScreenImpl(gui));
        }

        public void fillQuad(DrawContextWrapper drawContextWrapper, GraphicsQuad<PosXY> quad, int color, float z) {
            PosXY v0 = quad.getVertex(0), v1 = quad.getVertex(1), v2 = quad.getVertex(2), v3 = quad.getVertex(3);

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

        @Override
        public void drawNativeImage(DrawContextWrapper drawContextWrapper, Object allocatedTextureObject, int x, int y, int w, int h) {
            GlStateManager.color(1, 1, 1, 1);
            super.drawNativeImage(drawContextWrapper, allocatedTextureObject, x, y, w, h);
        }

        public void drawButton(DrawContextWrapper drawContextWrapper, int x, int y, int width, int height, AbstractWidgetCopy.HoverState hoverState) {
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

        public void drawCheckBox(DrawContextWrapper drawContextWrapper, int x, int y, int width, int height, boolean focused, boolean checked) {
            drawButton(drawContextWrapper, x, y, width, height, AbstractWidgetCopy.HoverState.DISABLED);
            if (checked) {
                FontRenderer.DEFAULT.drawCenteredStringWithShadow(drawContextWrapper, "x", x + width / 2f + 1, y + 1,
                        GuiEventListenerCopy.NORMAL_TEXT_COLOR);
            }
        }

        public void drawTextFieldHighlight(DrawContextWrapper drawContextWrapper, int startX, int startY, int endX, int endY) {
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuffer();
            GlStateManager.color(0.0F, 0.0F, 255.0F, 255.0F);
            GlStateManager.disableTexture2D();
            GlStateManager.enableColorLogic();
            GlStateManager.colorLogicOp(GlStateManager.LogicOp.OR_REVERSE);
            bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
            bufferbuilder.pos(startX, endY, 0.0D).endVertex();
            bufferbuilder.pos(endX, endY, 0.0D).endVertex();
            bufferbuilder.pos(endX, startY, 0.0D).endVertex();
            bufferbuilder.pos(startX, startY, 0.0D).endVertex();
            tessellator.draw();
            GlStateManager.disableColorLogic();
            GlStateManager.enableTexture2D();
        }

        public void drawImage(DrawContextWrapper drawContextWrapper, ResourceLocationWrapper res, int x, int y, int w, int h, float u1, float v1, float u2, float v2) {
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
            GlStateManager.color(1, 1, 1, 1);

            ResourceLocation resourceLocation = res.get();
            Minecraft.getMinecraft().renderEngine.bindTexture(resourceLocation);

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuffer();
            bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
            bufferbuilder.pos(x, y+h, 0).tex(u1, v2).endVertex();
            bufferbuilder.pos(x+w, y+h, 0).tex(u2, v2).endVertex();
            bufferbuilder.pos(x+w, y, 0).tex(u2, v1).endVertex();
            bufferbuilder.pos(x, y, 0).tex(u1, v1).endVertex();
            tessellator.draw();
        }
    };}
}
