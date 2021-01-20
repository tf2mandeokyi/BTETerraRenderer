package com.mndk.mapdisp4bte.gui;

import com.mndk.mapdisp4bte.ModConfig;
import com.mndk.mapdisp4bte.ModReference;
import com.mndk.mapdisp4bte.gui.option.GuiOptionsList;
import com.mndk.mapdisp4bte.gui.option.slider.GuiNumberSlider;
import com.mndk.mapdisp4bte.gui.option.toggleable.GuiBooleanToggleable;
import com.mndk.mapdisp4bte.gui.option.toggleable.GuiEnumToggleable;
import com.mndk.mapdisp4bte.map.RenderMapSource;
import com.mndk.mapdisp4bte.map.RenderMapType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.io.IOException;

public class MapRenderingOptionsUI extends GuiScreen {

    private static final int TITLE_HEIGHT = 8;
    private static final int OPTIONS_LIST_TOP_MARGIN = 24;
    private static final int BUTTON_TOP_MARGIN = 5;

    private static final int BUTTON_WIDTH = 150;
    private static final int BUTTON_HEIGHT = 20;
    private static final int DONE_BUTTON_BOTTOM_MARGIN = 26;

    private static final int SETTINGS_CENTER_X = 150;

    private static final int ALIGNMENT_IMAGE_MARGIN_BOTTOM = 20;
    private static final int ALIGNMENT_IMAGE_MARGIN_RIGHT = 20;
    private static final int ALIGNMENT_IMAGE_WIDTH = 128;
    private static final int ALIGNMENT_IMAGE_HEIGHT = 128;

    private static final int ALIGNMENT_RESET_BUTTON_WIDTH = 50;
    private static final int ALIGNMENT_RESET_BUTTON_HEIGHT = 20;

    private static final int MIN_IMAGE_ALIGNMENT_VALUE = -20;
    private static final int MAX_IMAGE_ALIGNMENT_VALUE = 20;
    private static final int IMAGE_ALIGNMENT_VALUE_RANGE = MAX_IMAGE_ALIGNMENT_VALUE - MIN_IMAGE_ALIGNMENT_VALUE;

    GuiButton doneButton, xAlignResetButton, zAlignResetButton;
    GuiOptionsList optionsList;

    private static final ResourceLocation ALIGNMENT_IMAGE_RELOC =
            new ResourceLocation(ModReference.MODID, "textures/ui/alignment_image.png");
    private static final ResourceLocation ALIGNMENT_MARKER_RELOC =
            new ResourceLocation(ModReference.MODID, "textures/ui/alignment_marker.png");

    private boolean mouseClickedInAlignmentImage = false;



    @Override
    public void initGui() {
        Minecraft.getMinecraft().mouseHelper.ungrabMouseCursor();

        super.initGui();

        this.setupOptionsList();
        this.addOtherButtons();
    }



    private void addOtherButtons() {
        this.addButton(doneButton = new GuiButton(
                0,
                SETTINGS_CENTER_X - (BUTTON_WIDTH / 2), this.height - DONE_BUTTON_BOTTOM_MARGIN,
                BUTTON_WIDTH, BUTTON_HEIGHT,
                I18n.format("gui.done")
        ));

        this.addButton(xAlignResetButton = new GuiButton(
                1000,
                this.width - ALIGNMENT_IMAGE_MARGIN_RIGHT - ALIGNMENT_RESET_BUTTON_WIDTH,
                this.height - ALIGNMENT_IMAGE_MARGIN_BOTTOM - ALIGNMENT_IMAGE_HEIGHT - ALIGNMENT_RESET_BUTTON_HEIGHT - 5,
                ALIGNMENT_RESET_BUTTON_WIDTH,
                ALIGNMENT_RESET_BUTTON_HEIGHT,
                I18n.format("gui.mapdisp4bte.maprenderer.x_align_reset")
        ));

        this.addButton(zAlignResetButton = new GuiButton(
                1001,
                this.width - ALIGNMENT_IMAGE_MARGIN_RIGHT - ALIGNMENT_IMAGE_WIDTH - ALIGNMENT_RESET_BUTTON_WIDTH - 5,
                this.height - ALIGNMENT_IMAGE_MARGIN_BOTTOM - ((ALIGNMENT_IMAGE_HEIGHT + ALIGNMENT_RESET_BUTTON_HEIGHT) / 2),
                ALIGNMENT_RESET_BUTTON_WIDTH,
                ALIGNMENT_RESET_BUTTON_HEIGHT,
                I18n.format("gui.mapdisp4bte.maprenderer.z_align_reset")
        ));
    }



    private void setupOptionsList() {
        this.optionsList = new GuiOptionsList(this,
                SETTINGS_CENTER_X - (BUTTON_WIDTH / 2), OPTIONS_LIST_TOP_MARGIN,
                BUTTON_WIDTH, BUTTON_HEIGHT, BUTTON_TOP_MARGIN
        );

        this.optionsList.addToggleable(new GuiBooleanToggleable(
                () -> ModConfig.drawTiles, (b) -> ModConfig.drawTiles = b,
                I18n.format("gui.mapdisp4bte.maprenderer.enable_render")
        ));

        this.optionsList.addToggleable(new GuiEnumToggleable<>(
                () -> RenderMapType.valueOf(ModConfig.mapType), (e) -> ModConfig.mapType = e.toString(),
                RenderMapType.values(),
                I18n.format("gui.mapdisp4bte.maprenderer.map_type")
        ));

        this.optionsList.addToggleable(new GuiEnumToggleable<>(
                () -> RenderMapSource.valueOf(ModConfig.mapSource), (e) -> ModConfig.mapSource = e.toString(),
                RenderMapSource.values(),
                I18n.format("gui.mapdisp4bte.maprenderer.map_source")
        ));

        this.optionsList.addSlider(new GuiNumberSlider<>(
                () -> ModConfig.yLevel, (n) -> ModConfig.yLevel = n,
                0., 256.,
                I18n.format("gui.mapdisp4bte.maprenderer.map_y_level")
        ));

        this.optionsList.addSlider(new GuiNumberSlider<>(
                () -> ModConfig.opacity, (n) -> ModConfig.opacity = n,
                0., 1.,
                I18n.format("gui.mapdisp4bte.maprenderer.opacity")
        ));

        for(GuiButton button : optionsList.buttons) {
            this.addButton(button);
        }
    }



    @Override
    protected void actionPerformed(GuiButton button) {
        if(button == doneButton) { // Done button
            Minecraft.getMinecraft().player.closeScreen();
        }
        else if(button == xAlignResetButton) {
            ModConfig.xAlign = 0;
        }
        else if(button == zAlignResetButton) {
            ModConfig.zAlign = 0;
        }
        optionsList.actionPerformed(button);
    }



    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawCenteredString(
                this.fontRenderer, I18n.format("gui.mapdisp4bte.maprenderer.title"),
                SETTINGS_CENTER_X, TITLE_HEIGHT, 0xFFFFFF
        );

        String xAlignString = I18n.format("gui.mapdisp4bte.maprenderer.x_align") + ": " + ModConfig.xAlign + "m";
        String zAlignString = I18n.format("gui.mapdisp4bte.maprenderer.z_align") + ": " + ModConfig.zAlign + "m";

        int imageRight = this.width - ALIGNMENT_IMAGE_MARGIN_RIGHT, imageLeft = imageRight - ALIGNMENT_IMAGE_WIDTH;
        int imageBottom = this.height - ALIGNMENT_IMAGE_HEIGHT, imageTop = imageBottom - ALIGNMENT_IMAGE_MARGIN_BOTTOM;

        this.drawCenteredString(
                this.fontRenderer, I18n.format("gui.mapdisp4bte.maprenderer.map_align"),
                imageLeft + (ALIGNMENT_IMAGE_WIDTH / 2),
                imageTop - ALIGNMENT_RESET_BUTTON_HEIGHT - this.fontRenderer.FONT_HEIGHT - 10,
                0xFFFFFF
        );

        // X Align
        this.drawString(
                this.fontRenderer, xAlignString,
                imageRight - ALIGNMENT_RESET_BUTTON_WIDTH - this.fontRenderer.getStringWidth(xAlignString) - 5,
                imageTop - (this.fontRenderer.FONT_HEIGHT + ALIGNMENT_RESET_BUTTON_HEIGHT) / 2 - 5,
                0xFFFFFF
        );

        // Z Align
        this.drawString(
                this.fontRenderer, zAlignString,
                imageLeft - ALIGNMENT_RESET_BUTTON_WIDTH - this.fontRenderer.getStringWidth(zAlignString) - 10,
                imageTop + ((ALIGNMENT_IMAGE_HEIGHT - this.fontRenderer.FONT_HEIGHT) / 2),
                0xFFFFFF
        );

        // Alignment image
        Minecraft.getMinecraft().renderEngine.bindTexture(ALIGNMENT_IMAGE_RELOC);
        this.drawImage(imageLeft, imageTop, ALIGNMENT_IMAGE_WIDTH, ALIGNMENT_IMAGE_HEIGHT);

        // Alignment marker
        double x1 = - ALIGNMENT_IMAGE_WIDTH * (ModConfig.xAlign - MAX_IMAGE_ALIGNMENT_VALUE) / IMAGE_ALIGNMENT_VALUE_RANGE,
                y1 = - ALIGNMENT_IMAGE_HEIGHT * (ModConfig.zAlign - MAX_IMAGE_ALIGNMENT_VALUE) / IMAGE_ALIGNMENT_VALUE_RANGE;
        int marker_pos_x = (int) (x1 + this.width - ALIGNMENT_IMAGE_MARGIN_RIGHT - ALIGNMENT_IMAGE_WIDTH),
                marker_pos_y = (int) (y1 + this.height - ALIGNMENT_IMAGE_MARGIN_BOTTOM - ALIGNMENT_IMAGE_HEIGHT);


        Minecraft.getMinecraft().renderEngine.bindTexture(ALIGNMENT_MARKER_RELOC);
        this.drawCenteredImage(marker_pos_x, marker_pos_y, 4, 4);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }



    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }



    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if(this.mouseClickedInAlignmentImage = this.isMouseInAlignmentImage(mouseX, mouseY)) {
            mouseXYToXZAlign(mouseX, mouseY);
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }



    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if(mouseClickedInAlignmentImage) {
            if(this.isMouseInAlignmentImage(mouseX, mouseY)) {
                mouseXYToXZAlign(mouseX, mouseY);
            }
        }
    }



    private boolean isMouseInAlignmentImage(int mouseX, int mouseY) {
        return mouseX >= this.width - ALIGNMENT_IMAGE_MARGIN_RIGHT - ALIGNMENT_IMAGE_WIDTH &&
               mouseX <= this.width - ALIGNMENT_IMAGE_MARGIN_RIGHT &&
               mouseY >= this.height - ALIGNMENT_IMAGE_MARGIN_BOTTOM - ALIGNMENT_IMAGE_HEIGHT &&
               mouseY <= this.height - ALIGNMENT_IMAGE_MARGIN_BOTTOM;
    }



    private void mouseXYToXZAlign(int mouseX, int mouseY) {
        int x1 = mouseX - this.width + ALIGNMENT_IMAGE_MARGIN_RIGHT + ALIGNMENT_IMAGE_WIDTH,
                y1 = mouseY - this.height + ALIGNMENT_IMAGE_MARGIN_BOTTOM + ALIGNMENT_IMAGE_HEIGHT;
        ModConfig.xAlign = MAX_IMAGE_ALIGNMENT_VALUE - IMAGE_ALIGNMENT_VALUE_RANGE * x1 / (float) ALIGNMENT_IMAGE_WIDTH;
        ModConfig.zAlign = MAX_IMAGE_ALIGNMENT_VALUE - IMAGE_ALIGNMENT_VALUE_RANGE * y1 / (float) ALIGNMENT_IMAGE_HEIGHT;
    }



    private void drawImage(int x, int y, int w, int h) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos(x, y+h, this.zLevel).tex(0, 1).endVertex();
        bufferbuilder.pos(x+w, y+h, this.zLevel).tex(1, 1).endVertex();
        bufferbuilder.pos(x+w, y, this.zLevel).tex(1, 0).endVertex();
        bufferbuilder.pos(x, y, this.zLevel).tex(0, 0).endVertex();
        tessellator.draw();
    }



    private void drawCenteredImage(int x, int y, int w, int h) {
        this.drawImage(x - w/2, y - h/2, w, h);
    }



}
