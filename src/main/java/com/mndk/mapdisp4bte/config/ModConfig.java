package com.mndk.mapdisp4bte.config;

import com.mndk.mapdisp4bte.map.RenderMapSource;
import com.mndk.mapdisp4bte.map.RenderMapType;
import org.yaml.snakeyaml.Yaml;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ModConfig {

    private AlignmentAxis align;
    private double yLevel;
    private String mapSource;
    private String mapType;
    private double opacity;
    private boolean drawTiles;
    private int zoom;


    public static class AlignmentAxis {
        public double x, z;
        public AlignmentAxis(double x, double z) {
            this.x = x; this.z = z;
        }
    }


    public ModConfig() {
        this.align = new AlignmentAxis(0, 0);
        this.drawTiles = false;
        this.yLevel = 4;
        this.mapSource = "OSM";
        this.mapType = "PLAIN_MAP";
        this.opacity = 0.7;
        this.zoom = 0;
    }


    @SuppressWarnings("unchecked")
    public ModConfig(Map<String, Object> map) {
    	this();
        Map<String, Object> alignMap = (Map<String, Object>) map.get("align");
        if(alignMap != null) if(alignMap.containsKey("x") && alignMap.containsKey("z")) 
        	this.align = new AlignmentAxis((double) alignMap.get("x"), (double) alignMap.get("z"));
        if(map.containsKey("draw"))
        	this.drawTiles = (boolean) map.get("draw");
        if(map.containsKey("y_level"))
        	this.yLevel = (double) map.get("y_level");
        if(map.containsKey("map_source"))
        	this.mapSource = (String) map.get("map_source");
        if(map.containsKey("map_type"))
        	this.mapType = (String) map.get("map_type");
        if(map.containsKey("opacity"))
        	this.opacity = (double) map.get("opacity");
        if(map.containsKey("zoom"))
        	this.zoom = (int) map.get("zoom");
        // TODO simplify these with annotation or smth
    }


    public void saveTo(Yaml yaml, FileWriter fileWriter) throws IOException {
        Map<String, Object> map = new HashMap<>();
        map.put("align", new HashMap<String, Object>() {{
            put("x", align.x);
            put("z", align.z);
        }});
        map.put("draw", drawTiles);
        map.put("y_level", yLevel);
        map.put("map_source", mapSource);
        map.put("map_type", mapType);
        map.put("opacity", opacity);
        map.put("zoom", zoom);

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
        return RenderMapSource.valueOf(mapSource);
    }

    public void setMapSource(RenderMapSource mapSource) {
        this.mapSource = mapSource.toString();
    }

    public RenderMapType getMapType() {
        return RenderMapType.valueOf(mapType);
    }

    public void setMapType(RenderMapType mapType) {
        this.mapType = mapType.toString();
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

    public boolean isDrawTiles() {
        return drawTiles;
    }

    public void setDrawTiles(boolean b) {
        drawTiles = b;
    }

    public void toggleDrawTiles() {
        drawTiles = !drawTiles;
    }
}
