package com.mndk.mapdisp4bte.gui;

import com.mndk.mapdisp4bte.MapDisplayer4BTE;
import com.mndk.mapdisp4bte.gui.option.toggleable.GuiEnumToggleable;
import com.mndk.mapdisp4bte.util.TranslatableEnum;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;

public class GuiEnumSelectionUi<T extends TranslatableEnum<T>> extends GuiScreen {



    private static final int TITLE_HEIGHT = 8;
    private static final int LIST_TOP_MARGIN = 40;
    private static final int ELEMENT_TOP_MARGIN = 10;
    private static final int LIST_LEFT_MARGIN = 95;

    private static final int BUTTON_WIDTH = 150;
    private static final int BUTTON_HEIGHT = 20;
    private static final int DONE_BUTTON_BOTTOM_MARGIN = 26;

    private static final int SETTINGS_CENTER_X = 170;

    private static final ResourceLocation RADIO_BUTTON_IMAGE =
            new ResourceLocation(MapDisplayer4BTE.MODID, "textures/ui/radio_button.png");

    private GuiButton doneButton;

    private final GuiEnumToggleable<T> option;



    public GuiEnumSelectionUi(GuiEnumToggleable<T> option) {
        this.option = option;
    }



    @Override
    public void initGui() {
        super.initGui();

        this.addButton(doneButton = new GuiButton(
                0,
                SETTINGS_CENTER_X - (BUTTON_WIDTH / 2), this.height - DONE_BUTTON_BOTTOM_MARGIN,
                BUTTON_WIDTH, BUTTON_HEIGHT,
                I18n.format("gui.done")
        ));
    }



    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawCenteredString(
                this.fontRenderer, option.name,
                SETTINGS_CENTER_X, TITLE_HEIGHT, 0xFFFFFF
        );

        for(int i=0;i<this.option.list.length;i++) {
            T element = this.option.list[i];
            float u = (option.get() == element ? 1/8.f : 0) + (isMouseOnIndex(mouseX, mouseY, i) ? 1/16.f : 0);
            ImageUiRenderer.drawImage(RADIO_BUTTON_IMAGE,
                    LIST_LEFT_MARGIN,
                    LIST_TOP_MARGIN + (this.fontRenderer.FONT_HEIGHT + ELEMENT_TOP_MARGIN) * i - 8,
                    0,
                    16, 16,
                    u, 0, u + 1/16.f, 1/16.f);
            this.drawString(this.fontRenderer, element.getTranslatedString(),
                    LIST_LEFT_MARGIN + 20,
                    LIST_TOP_MARGIN + (this.fontRenderer.FONT_HEIGHT + ELEMENT_TOP_MARGIN) * i - (this.fontRenderer.FONT_HEIGHT / 2),
                    0xFFFFFF);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }



    private boolean isMouseOnIndex(int mouseX, int mouseY, int index) {
        int y = LIST_TOP_MARGIN + (this.fontRenderer.FONT_HEIGHT + ELEMENT_TOP_MARGIN) * index;
        return
                mouseX >= LIST_LEFT_MARGIN &&
                mouseY >= y - 8 &&
                mouseX < LIST_LEFT_MARGIN + this.fontRenderer.getStringWidth(option.list[index].getTranslatedString()) &&
                mouseY < y + 8;
    }



    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if(button == doneButton) {
            Minecraft.getMinecraft().displayGuiScreen(new MapRenderingOptionsUi());
        }
        super.actionPerformed(button);
    }


    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        for(int i=0;i<this.option.list.length;i++) {
            if(isMouseOnIndex(mouseX, mouseY, i)) {
                option.set(option.list[i]);
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
