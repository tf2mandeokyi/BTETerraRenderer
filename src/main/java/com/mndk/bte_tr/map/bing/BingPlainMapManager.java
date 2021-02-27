package com.mndk.bte_tr.map.bing;

import com.mndk.bte_tr.map.RenderMapSource;

public class BingPlainMapManager extends BingMapManager {
    public BingPlainMapManager() { 
    	super(RenderMapSource.BING_PLAIN, "https://t.ssl.ak.dynamic.tiles.virtualearth.net/comp/ch/{u}?it=G,LC,BX,RL&shading=hill"); 
    }
}
