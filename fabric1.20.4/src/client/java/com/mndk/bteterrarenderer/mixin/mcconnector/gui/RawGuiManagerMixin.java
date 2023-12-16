package com.mndk.bteterrarenderer.mixin.mcconnector.gui;

import com.mndk.bteterrarenderer.mcconnector.IResourceLocation;
import com.mndk.bteterrarenderer.mcconnector.gui.RawGuiManager;
import com.mndk.bteterrarenderer.mcconnector.gui.component.AbstractGuiScreenCopy;
import com.mndk.bteterrarenderer.mcconnector.gui.component.GuiAbstractWidgetCopy;
import com.mndk.bteterrarenderer.mod.client.gui.AbstractGuiScreenImpl;
import com.mndk.bteterrarenderer.mod.mcconnector.IResourceLocationIdentifierImpl;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.experimental.UtilityClass;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.render.*;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

@UtilityClass
@Mixin(value = RawGuiManager.class, remap = false)
public class RawGuiManagerMixin {

    @Unique
    private final Identifier CHECKBOX_SELECTED_HIGHLIGHTED = new Identifier("widget/checkbox_selected_highlighted");
    @Unique
    private final Identifier CHECKBOX_SELECTED = new Identifier("widget/checkbox_selected");
    @Unique
    private final Identifier CHECKBOX_HIGHLIGHTED = new Identifier("widget/checkbox_highlighted");
    @Unique
    private final Identifier CHECKBOX = new Identifier("widget/checkbox");
    @Unique
    private final ButtonTextures BUTTON_TEXTURES = new ButtonTextures(
            new Identifier("widget/button"),
            new Identifier("widget/button_disabled"),
            new Identifier("widget/button_highlighted")
    );

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    private static RawGuiManager<DrawContext> makeInstance() { return new RawGuiManager<>() {
        public void displayGuiScreen(AbstractGuiScreenCopy gui) {
            MinecraftClient.getInstance().setScreen(new AbstractGuiScreenImpl(gui));
        }

        public void drawButton(DrawContext drawContext, int x, int y, int width, int height, GuiAbstractWidgetCopy.HoverState hoverState) {
            boolean enabled = hoverState != GuiAbstractWidgetCopy.HoverState.DISABLED;
            boolean focused = hoverState == GuiAbstractWidgetCopy.HoverState.MOUSE_OVER;
            Identifier buttonTexture = BUTTON_TEXTURES.get(enabled, focused);

            drawContext.drawGuiTexture(buttonTexture, x, y, width, height);
        }

        public void drawCheckBox(DrawContext drawContext, int x, int y, int width, int height, boolean focused, boolean checked) {RenderSystem.setShader(GameRenderer::getPositionTexProgram);
            RenderSystem.enableDepthTest();
            RenderSystem.enableBlend();

            Identifier identifier = checked ?
                    (focused ? CHECKBOX_SELECTED_HIGHLIGHTED : CHECKBOX_SELECTED) :
                    (focused ? CHECKBOX_HIGHLIGHTED : CHECKBOX);

            drawContext.setShaderColor(1, 1, 1, 1);
            drawContext.drawGuiTexture(identifier, x, y, width, height);
        }

        public void drawTextFieldHighlight(DrawContext drawContext, int startX, int startY, int endX, int endY) {
            RenderSystem.setShader(GameRenderer::getPositionProgram);
            RenderSystem.setShaderColor(0.0F, 0.0F, 1.0F, 1.0F);
            RenderSystem.enableColorLogicOp();
            RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE);

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuffer();

            Matrix4f matrix = drawContext.getMatrices().peek().getPositionMatrix();
            bufferbuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
            bufferbuilder.vertex(matrix, startX, endY, 0).next();
            bufferbuilder.vertex(matrix, endX, endY, 0).next();
            bufferbuilder.vertex(matrix, endX, startY, 0).next();
            bufferbuilder.vertex(matrix, startX, startY, 0).next();
            tessellator.draw();

            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.disableColorLogicOp();
        }

        public void drawImage(DrawContext drawContext, IResourceLocation res, int x, int y, int w, int h, float u1, float v1, float u2, float v2) {
            RenderSystem.setShader(GameRenderer::getPositionTexProgram);
            RenderSystem.setShaderTexture(0, ((IResourceLocationIdentifierImpl) res).delegate());
            RenderSystem.setShaderColor(1, 1, 1, 1);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableDepthTest();

            BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();
            Matrix4f matrix = drawContext.getMatrices().peek().getPositionMatrix();
            drawBufferPosTex(bufferbuilder, matrix, x, y, w, h, u1, v1, u2, v2);
        }

        private void drawBufferPosTex(BufferBuilder bufferBuilder,
                                     Matrix4f matrix,
                                     int x, int y, int w, int h,
                                     float u0, float v0, float u1, float v1) {
            bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
            bufferBuilder.vertex(matrix, x, y+h, 0).texture(u0, v1).next();
            bufferBuilder.vertex(matrix, x+w, y+h, 0).texture(u1, v1).next();
            bufferBuilder.vertex(matrix, x+w, y, 0).texture(u1, v0).next();
            bufferBuilder.vertex(matrix, x, y, 0).texture(u0, v0).next();
            BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        }
    };}

}
