package com.mndk.bteterrarenderer.mixin.mcconnector.wrapper;

import com.mndk.bteterrarenderer.mcconnector.gui.widget.AbstractWidgetCopy;
import com.mndk.bteterrarenderer.mcconnector.wrapper.*;
import com.mndk.bteterrarenderer.mod.mcconnector.gui.AbstractGuiScreenImpl;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector4f;
import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

import javax.annotation.Nonnull;

@UtilityClass
@Mixin(value = DrawContextWrapper.class, remap = false)
public class DrawContextWrapperMixin {

    @Unique
    private final ResourceLocation bTETerraRenderer$CHECKBOX = new ResourceLocation("textures/gui/checkbox.png");

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public static DrawContextWrapper<?> of(@Nonnull Object delegate) { return new DrawContextWrapper<PoseStack>(delegate) {

        public void translate(float x, float y, float z) {
            getThisWrapped().translate(x, y, z);
        }
        public void pushMatrix() {
            getThisWrapped().pushPose();
        }
        public void popMatrix() {
            getThisWrapped().popPose();
        }

        public int[] getAbsoluteScissorDimension(int relX, int relY, int relWidth, int relHeight) {
            Window window = Minecraft.getInstance().getWindow();
            if(window.getScreenWidth() == 0 || window.getScreenHeight() == 0) { // Division by zero handling
                return new int[] { 0, 0, 0, 0 };
            }
            float scaleFactorX = (float) window.getScreenWidth() / window.getGuiScaledWidth();
            float scaleFactorY = (float) window.getScreenHeight() / window.getGuiScaledHeight();

            Matrix4f matrix = getThisWrapped().last().pose();
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

        public void drawButton(int x, int y, int width, int height, AbstractWidgetCopy.HoverState hoverState) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, AbstractWidget.WIDGETS_LOCATION);
            RenderSystem.setShaderColor(1, 1, 1, 1);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableDepthTest();

            int i = switch (hoverState) {
                case DISABLED -> 0;
                case DEFAULT -> 1;
                case MOUSE_OVER -> 2;
            };

            PoseStack poseStack = getThisWrapped();
            GuiComponent.blit(poseStack, x, y, 0, 0, 46 + i * 20, width / 2, height, 256, 256);
            GuiComponent.blit(poseStack, x + width / 2, y, 0, 200 - (float) width / 2, 46 + i * 20, width / 2, height, 256, 256);
        }

        public void drawCheckBox(int x, int y, int width, int height, boolean focused, boolean checked) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, bTETerraRenderer$CHECKBOX);
            RenderSystem.enableDepthTest();
            RenderSystem.setShaderColor(1, 1, 1, 1);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

            float size = 20 / 64f;
            float u1 = focused ? size : 0, v1 = checked ? size : 0;
            float u2 = u1 + size, v2 = v1 + size;
            this.drawPosTexQuad(x, y, width, height, u1, v1, u2, v2);
        }

        public void drawTextHighlight(int startX, int startY, int endX, int endY) {
            RenderSystem.setShader(GameRenderer::getPositionShader);
            RenderSystem.setShaderColor(0.0F, 0.0F, 1.0F, 1.0F);
            RenderSystem.disableTexture();
            RenderSystem.enableColorLogicOp();
            RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE);

            this.drawPosQuad(startX, startY, endX - startX, endY - startY);

            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.disableColorLogicOp();
            RenderSystem.enableTexture();
        }

        public void drawImage(ResourceLocationWrapper<?> res, int x, int y, int w, int h, float u1, float v1, float u2, float v2) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, res.get());
            RenderSystem.setShaderColor(1, 1, 1, 1);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableDepthTest();
            this.drawPosTexQuad(x, y, w, h, u1, v1, u2, v2);
        }

        public void drawHoverEvent(StyleWrapper styleWrapper, int x, int y) {
            Screen currentScreen = Minecraft.getInstance().screen;
            if(!(currentScreen instanceof AbstractGuiScreenImpl guiScreen)) return;

            Style style = styleWrapper.get();
            guiScreen.renderComponentHoverEffect(getThisWrapped(), style, x, y);
        }

        public int drawTextWithShadow(FontWrapper<?> fontWrapper, String string, float x, float y, int color) {
            Font font = fontWrapper.get();
            return font.drawShadow(getThisWrapped(), string, x, y, color);
        }

        public int drawTextWithShadow(FontWrapper<?> fontWrapper, TextWrapper textWrapper, float x, float y, int color) {
            Font font = fontWrapper.get();
            Object textComponent = textWrapper.get();
            if(textComponent instanceof Component component) {
                return font.drawShadow(getThisWrapped(), component, x, y, color);
            }
            else if(textComponent instanceof FormattedCharSequence sequence) {
                return font.drawShadow(getThisWrapped(), sequence, x, y, color);
            }
            return 0;
        }
    };}

}
