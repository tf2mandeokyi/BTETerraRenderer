package com.mndk.mapdisp4bte;

import net.minecraftforge.common.config.Config;

@Config(modid = ModReference.MODID, type = Config.Type.INSTANCE, category = "general")
public class ModConfig {


    @Config.Comment("The map alignment value in x axis")
    @Config.Name("X Align")
    public static double xAlign = 0;


    @Config.Comment("The map alignment value in z axis")
    @Config.Name("Z Align")
    public static double zAlign = 0;


    @Config.Comment("The y level that the map should be rendered")
    @Config.Name("Y Level")
    public static double yLevel = 4;



    @Config.Comment("True if the map should be rendered, and false otherwise.")
    @Config.Name("Render Map")
    public static boolean drawTiles = false;



    @Config.Comment({
            "The source of the map",
            "List: KAKAO for KakaoMap, OSM for OpenStreetMap, BING for Bing maps, and TMAP for Tmap"
    })
    @Config.Name("Map Source")
    public static String mapSource = "KAKAO";



    @Config.Comment({
            "The type of the map",
            "List: PLAIN_MAP for normal map, and AERIAL for aerial map"
    })
    @Config.Name("Map Type")
    public static String mapType = "PLAIN_MAP";



    @Config.Comment("The opacity of the map")
    @Config.Name("Map Opacity")
    @Config.RangeDouble(min = 0, max = 1)
    public static double opacity = 0.7;
}
