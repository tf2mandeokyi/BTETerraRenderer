package com.mndk.bte_tr.config;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import com.mndk.bte_tr.map.RenderMapSource;

public class ModConfig {

    private AlignmentAxis align;
    private double yLevel;
    private RenderMapSource mapSource;
    private double opacity;
    private boolean renderTiles;
    private int zoom;
    private int radius;


    public static class AlignmentAxis {
        public double x, z;
        public AlignmentAxis(double x, double z) {
            this.x = x; this.z = z;
        }
    }


    public ModConfig() {
        this.align = new AlignmentAxis(0, 0);
        this.renderTiles = false;
        this.yLevel = 4;
        this.mapSource = RenderMapSource.OSM;
        this.opacity = 0.7;
        this.zoom = 0;
        this.radius = 2;
    }


    @SuppressWarnings("unchecked")
    public ModConfig(Map<String, Object> map) {
    	this();
    	
        Map<String, Object> alignMap = (Map<String, Object>) map.get("align");
        if(alignMap != null) {
        	if(alignMap.containsKey("x") && alignMap.containsKey("z")) {
            	this.align = new AlignmentAxis((double) alignMap.get("x"), (double) alignMap.get("z"));
        	}
        }
        
        if(map.containsKey("draw")) this.renderTiles = (boolean) map.get("draw");

        if(map.containsKey("y_level")) this.yLevel = (double) map.get("y_level");

        try {
        	if(map.containsKey("map_source")) this.mapSource = RenderMapSource.valueOf((String) map.get("map_source"));
        } catch(Exception ignored) {}

        if(map.containsKey("opacity")) this.opacity = (double) map.get("opacity");

        if(map.containsKey("zoom")) this.zoom = (int) map.get("zoom");
        
        if(map.containsKey("radius")) this.radius = (int) map.get("radius");
        // TODO simplify these with annotations or smth
    }


    @SuppressWarnings("serial")
	public void saveTo(Yaml yaml, FileWriter fileWriter) throws IOException {
        Map<String, Object> map = new HashMap<>();
        map.put("align", new HashMap<String, Object>() {{
            put("x", align.x);
            put("z", align.z);
        }});
        map.put("draw", renderTiles);
        map.put("y_level", yLevel);
        map.put("map_source", mapSource.toString());
        map.put("opacity", opacity);
        map.put("zoom", zoom);
        map.put("radius", radius);

        yaml.dump(map, fileWriter);
        fileWriter.close();
    }


    public double getXAlign() {
        return align.x;
    }

    public void setXAlign(double x) {
        align.x = x;
    }

    public double getZAlign() {
        return align.z;
    }

    public void setZAlign(double z) {
        align.z = z;
    }

    public double getYLevel() {
        return yLevel;
    }

    public void setYLevel(double yLevel) {
        this.yLevel = yLevel;
    }

    public RenderMapSource getMapSource() {
        return mapSource;
    }

    public void setMapSource(RenderMapSource mapSource) {
        this.mapSource = mapSource;
    }

    public double getOpacity() {
        return opacity;
    }

    public void setOpacity(double opacity) {
        this.opacity = opacity;
    }
    
    public int getZoom() {
    	return zoom;
    }
    
    public void setZoom(int zoom) {
    	this.zoom = zoom;
    }
    
    public int getRadius() {
    	return radius;
    }
    
    public void setRadius(int radius) {
    	this.radius = radius;
    }

    public boolean isTileRendering() {
        return renderTiles;
    }

    public void setTileRendering(boolean b) {
        renderTiles = b;
    }

    public void toggleTileRendering() {
        renderTiles = !renderTiles;
    }
}
