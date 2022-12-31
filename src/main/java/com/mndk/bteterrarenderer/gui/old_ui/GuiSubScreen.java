package com.mndk.bteterrarenderer.gui.old_ui;

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
	
	public abstract void actionPerformed(GuiButton button);
	
	public abstract void updateScreen();
	
	public abstract void drawScreen(int mouseX, int mouseY, float partialTicks);
	
	public abstract void mouseClicked(int mouseX, int mouseY, int mouseButton);
	
	public abstract void keyTyped(char key, int keyCode);
	
	public abstract void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick);
	
}
