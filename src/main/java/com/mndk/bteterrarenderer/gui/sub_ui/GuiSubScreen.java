package com.mndk.bteterrarenderer.gui.sub_ui;

import java.io.IOException;

import com.mndk.bteterrarenderer.gui.MapRenderingOptionsUI;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;

public abstract class GuiSubScreen extends Gui {

	protected final MapRenderingOptionsUI parent;
	protected FontRenderer fontRenderer;
	
	public GuiSubScreen(MapRenderingOptionsUI parent) {
		this.parent = parent;
	}
	
	public final void initGui() {
		this.fontRenderer = parent.getFontRenderer();
		this.init();
	}
	
	protected abstract void init();
	
	public abstract void actionPerformed(GuiButton button) throws IOException;
	
	public abstract void updateScreen();
	
	public abstract void drawScreen(int mouseX, int mouseY, float partialTicks);
	
	public abstract void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException;
	
	public abstract void keyTyped(char key, int keyCode) throws IOException;
	
	public abstract void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick);
	
}
