package com.mndk.mapdisp4bte.image;

import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;

import java.awt.image.BufferedImage;

@Deprecated
public class FixedGridedDynamicTextureManager {

    private final TextureManager textureManager;
    private DynamicTexture[][] textures;
    //private int[][][] textureData;
    private int xSize, ySize;
    private ResourceLocation[][] locations;

    public FixedGridedDynamicTextureManager(TextureManager textureManager, int xSize, int ySize) {
        this.textureManager = textureManager;
        this.xSize = xSize; this.ySize = ySize;
        this.textures = new DynamicTexture[ySize][xSize];
        //this.textureData = new int[ySize][xSize][];
        this.locations = new ResourceLocation[ySize][xSize];
    }



    public ResourceLocation setTextureData4ResourceLocation(int gridX, int gridY, BufferedImage image) {
        if (this.textures[gridY][gridX] != null) {
            this.textures[gridY][gridX].deleteGlTexture();
        }
        DynamicTexture texture = new DynamicTexture(image);
        this.textures[gridY][gridX] = texture;
        this.locations[gridY][gridX] = textureManager.getDynamicTextureLocation("texgrid_" + gridX + "_" + gridY, texture);
        return this.locations[gridY][gridX];
    }



    public ResourceLocation getResourceLocation(int gridX, int gridY) {
        return this.locations[gridY][gridX];
    }



    /*public int[] getTextureData(int gridX, int gridY) {
        return this.textureData[gridY][gridX];
    }*/



    /*
    private void updateTextureDataByImage(int gridX, int gridY, BufferedImage newImage) {
        //byte[] newImageData = ((DataBufferByte) newImage.getRaster().getDataBuffer()).getData();
        this.updateTextureData(gridX, gridY, newImage);
    }*/



        /*
    private void updateTextureData(int gridX, int gridY, BufferedImage newImage) {
        int width = newImage.getWidth();
        for(int i = 0; i < this.textureData[gridY][gridX].length; i++) {
            this.textureData[gridY][gridX][i] = newImage.getRGB(i % width, i / width);
        }
        this.textures[gridY][gridX].updateDynamicTexture();
        this.textures[gridY][gridX].deleteGlTexture();

        DynamicTexture texture = new DynamicTexture(newImage);
        this.textures[gridY][gridX] = texture;
        //this.textureData[gridY][gridX] = texture.getTextureData();
        this.locations[gridY][gridX] = textureManager.getDynamicTextureLocation("texgrid_" + gridX + "_" + gridY, texture);
    }
        */



    /*private void updateTextureData(int gridX, int gridY, int[] newTextureData) {
        if(this.textureData[gridY][gridX].length == newTextureData.length * 4) {
            for(int i = 0; i < this.textureData[gridY][gridX].length; i++) {
                this.textureData[gridY][gridX][i] = newTextureData[i];
            }
        }
        else {
            throw new ArrayStoreException("Array length mismatching.");
        }
        this.textures[gridY][gridX].updateDynamicTexture();
    }



    public void moveTextureData(int fromGridX, int fromGridY, int toGridX, int toGridY) {
        int[] temp = this.textureData[fromGridY][fromGridX];
        // ArrayOutofBoundsExecption handling
        if(toGridX < 0 || toGridX >= xSize || toGridY < 0 || toGridY >= ySize) return;
        this.updateTextureData(toGridX, toGridY, temp);
    }*/


}
