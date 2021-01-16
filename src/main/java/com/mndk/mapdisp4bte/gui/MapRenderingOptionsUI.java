package com.mndk.mapdisp4bte.gui;

import com.mndk.mapdisp4bte.ModConfig;
import com.mndk.mapdisp4bte.gui.option.GuiBooleanOption;
import com.mndk.mapdisp4bte.gui.option.GuiEnumOption;
import com.mndk.mapdisp4bte.gui.option.GuiNumberOption;
import com.mndk.mapdisp4bte.gui.option.GuiOptionsList;
import com.mndk.mapdisp4bte.map.RenderMapSource;
import com.mndk.mapdisp4bte.map.RenderMapType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

import java.io.IOException;

public class MapRenderingOptionsUI extends GuiScreen {

    private static final int TITLE_HEIGHT = 8;
    private static final int OPTIONS_LIST_TOP_MARGIN = 24;
    private static final int BUTTON_TOP_MARGIN = 5;

    private static final int BUTTON_WIDTH = 150;
    private static final int BUTTON_HEIGHT = 20;
    private static final int DONE_BUTTON_BOTTOM_MARGIN = 26;

    private static final int SETTINGS_CENTER_X = 150;

    GuiButton doneButton;
    GuiOptionsList options;

    @Override
    public void initGui() {
        Minecraft.getMinecraft().mouseHelper.ungrabMouseCursor();

        super.initGui();

        this.options = new GuiOptionsList(this,
                SETTINGS_CENTER_X - (BUTTON_WIDTH / 2), OPTIONS_LIST_TOP_MARGIN,
                BUTTON_WIDTH, BUTTON_HEIGHT, BUTTON_TOP_MARGIN
        );

        this.options.add(new GuiBooleanOption(
                () -> ModConfig.drawTiles, (b) -> ModConfig.drawTiles = b,
                I18n.format("gui.mapdisp4bte.maprenderer.enable_render")
        ));

        this.options.add(new GuiEnumOption<>(
                () -> RenderMapType.valueOf(ModConfig.mapType), (e) -> ModConfig.mapType = e.toString(),
                RenderMapType.values(), I18n.format("gui.mapdisp4bte.maprenderer.map_type")
        ));

        this.options.add(new GuiEnumOption<>(
                () -> RenderMapSource.valueOf(ModConfig.mapSource), (e) -> ModConfig.mapSource = e.toString(),
                RenderMapSource.values(), I18n.format("gui.mapdisp4bte.maprenderer.map_source")
        ));

        this.options.addSlider(new GuiNumberOption<>(
                () -> ModConfig.yLevel, (n) -> ModConfig.yLevel = n,
                0., 256.,
                I18n.format("gui.mapdisp4bte.maprenderer.map_y_level")
        ));

        this.options.addSlider(new GuiNumberOption<>(
                () -> ModConfig.opacity, (n) -> ModConfig.opacity = n,
                0., 1.,
                I18n.format("gui.mapdisp4bte.maprenderer.opacity")
        ));

        for(GuiButton button : options.buttons) {
            this.addButton(button);
        }

        this.addButton(doneButton = new GuiButton(
                0,
                SETTINGS_CENTER_X - (BUTTON_WIDTH / 2), this.height - DONE_BUTTON_BOTTOM_MARGIN,
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
        // this.drawDefaultBackground();
        this.drawCenteredString(
                this.fontRenderer, I18n.format("gui.mapdisp4bte.maprenderer.title"),
                SETTINGS_CENTER_X, TITLE_HEIGHT, 0xFFFFFF
        );
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

}
