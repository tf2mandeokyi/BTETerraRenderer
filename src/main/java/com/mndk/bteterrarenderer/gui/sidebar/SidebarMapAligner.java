package com.mndk.bteterrarenderer.gui.sidebar;

import com.mndk.bteterrarenderer.BTETerraRenderer;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;

public class SidebarMapAligner extends GuiSidebarElement {



    private static final int GRAY_AREA_HEIGHT = 40;



    private static final ResourceLocation ALIGNMENT_IMAGE_RELOC = new ResourceLocation(BTETerraRenderer.MODID,
            "textures/ui/alignment_image.png");
    private static final ResourceLocation ALIGNMENT_MARKER_RELOC = new ResourceLocation(BTETerraRenderer.MODID,
            "textures/ui/alignment_marker.png");



    @Override
    public int getHeight() {
        return 0;
    }

    @Override
    protected void init() {

    }

    @Override
    public void onWidthChange(int newWidth) {

    }

    @Override
    public void updateScreen() {

    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {

    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {

    }

    @Override
    public void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {

    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {

    }

    @Override
    public void keyTyped(char key, int keyCode) throws IOException {

    }
}
