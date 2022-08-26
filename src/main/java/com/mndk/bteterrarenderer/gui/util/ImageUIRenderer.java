package com.mndk.bteterrarenderer.gui.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class ImageUIRenderer {

	public static void drawImage(ResourceLocation res, int x, int y, float zLevel, int w, int h, float u1, float v1, float u2, float v2) {

		if(res != null) Minecraft.getMinecraft().renderEngine.bindTexture(res);

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();

		bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		bufferbuilder.pos(x, y+h, zLevel).tex(u1, v2).endVertex();
		bufferbuilder.pos(x+w, y+h, zLevel).tex(u2, v2).endVertex();
		bufferbuilder.pos(x+w, y, zLevel).tex(u2, v1).endVertex();
		bufferbuilder.pos(x, y, zLevel).tex(u1, v1).endVertex();

		tessellator.draw();
	}

	public static void drawImage(ResourceLocation res, int x, int y, float zLevel, int w, int h) {
		drawImage(res, x, y, zLevel, w, h, 0, 0, 1, 1);
	}



	public static void drawCenteredImage(ResourceLocation res, int x, int y, float zLevel, int w, int h) {
		drawImage(res, x - w/2, y - h/2, zLevel, w, h);
	}

}
