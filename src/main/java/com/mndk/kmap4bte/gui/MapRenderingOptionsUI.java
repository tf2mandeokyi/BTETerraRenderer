package com.mndk.kmap4bte.gui;

import com.mndk.kmap4bte.gui.option.GuiBooleanOption;
import com.mndk.kmap4bte.gui.option.GuiEnumOption;
import com.mndk.kmap4bte.gui.option.GuiNumberOption;
import com.mndk.kmap4bte.gui.option.GuiOptionsList;
import com.mndk.kmap4bte.map.RenderMapSource;
import com.mndk.kmap4bte.map.RenderMapType;
import com.mndk.kmap4bte.renderer.MapRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

import java.io.IOException;

public class MapRenderingOptionsUI extends GuiScreen {

    private static final int TITLE_HEIGHT = 8;
    private static final int OPTIONS_LIST_TOP_MARGIN = 24;
    private static final int BUTTON_TOP_MARGIN = 5;

    private static final int BUTTON_WIDTH = 256;
    private static final int BUTTON_HEIGHT = 20;
    private static final int DONE_BUTTON_BOTTOM_MARGIN = 26;

    GuiButton doneButton;
    GuiOptionsList options;

    @Override
    public void initGui() {
        Minecraft.getMinecraft().mouseHelper.ungrabMouseCursor();

        super.initGui();

        this.options = new GuiOptionsList(this,
                (this.width - BUTTON_WIDTH) / 2, OPTIONS_LIST_TOP_MARGIN,
                BUTTON_WIDTH, BUTTON_HEIGHT, BUTTON_TOP_MARGIN
        );

        this.options.add(new GuiBooleanOption(
                () -> MapRenderer.drawTiles, (b) -> MapRenderer.drawTiles = b,
                I18n.format("gui.kmap4bte.maprenderer.enable_render")
        ));
        System.out.println(I18n.hasKey("gui.kmap4bte.maprenderer.enable_render"));

        this.options.add(new GuiEnumOption<>(
                () -> MapRenderer.renderMapType, (e) -> MapRenderer.renderMapType = e,
                RenderMapType.PLAIN_MAP, RenderMapType.AERIAL,
                I18n.format("gui.kmap4bte.maprenderer.map_type")
        ));

        this.options.add(new GuiEnumOption<>(
                () -> MapRenderer.renderMapSource, (e) -> MapRenderer.renderMapSource = e,
                RenderMapSource.KAKAO, RenderMapSource.KAKAO,
                I18n.format("gui.kmap4bte.maprenderer.map_source")
        ));

        this.options.addSlider(new GuiNumberOption<Float>(
                () -> (int) MapRenderer.y + .0f, (n) -> MapRenderer.y = n + .1f,
                0.f, 256.f,
                I18n.format("gui.kmap4bte.maprenderer.map_y_level")
        ));

        for(GuiButton button : options.buttons) {
            this.addButton(button);
        }

        this.addButton(doneButton = new GuiButton(
                0,
                (this.width - BUTTON_WIDTH) / 2, this.height - DONE_BUTTON_BOTTOM_MARGIN,
                BUTTON_WIDTH, BUTTON_HEIGHT,
                I18n.format("gui.done")
        ));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if(button == doneButton) { // Done button
            Minecraft.getMinecraft().player.closeScreen();
        }
        options.actionPerformed(button);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRenderer, I18n.format("gui.kmap4bte.maprenderer.title"), this.width / 2, TITLE_HEIGHT, 0xFFFFFF);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

}
