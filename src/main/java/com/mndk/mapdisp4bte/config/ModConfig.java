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
    }


    @SuppressWarnings("unchecked")
    public ModConfig(Map<String, Object> map) {
        Map<String, Object> alignMap = (Map<String, Object>) map.get("align");
        this.align = new AlignmentAxis((double) alignMap.get("x"), (double) alignMap.get("z"));
        this.drawTiles = (boolean) map.get("draw");
        this.yLevel = (double) map.get("y_level");
        this.mapSource = (String) map.get("map_source");
        this.mapType = (String) map.get("map_type");
        this.opacity = (double) map.get("opacity");
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
