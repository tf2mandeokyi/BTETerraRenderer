package com.mndk.bte_tr.map.bing;

import com.mndk.bte_tr.map.RenderMapSource;

public class BingAerialMapManager extends BingMapManager {
    public BingAerialMapManager() { 
    	super(RenderMapSource.BING_AERIAL, "https://t.ssl.ak.dynamic.tiles.virtualearth.net/comp/ch/{u}?it=A&shading=hill"); 
    }
}
