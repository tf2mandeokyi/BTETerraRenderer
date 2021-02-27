package com.mndk.bte_tr.map.bing;

import com.mndk.bte_tr.map.RenderMapSource;
import com.mndk.bte_tr.map.mercator.MercatorMapManager;

public class BingMapManager extends MercatorMapManager {

    public BingMapManager(RenderMapSource source, String urlRequestTemplate) { super(source, urlRequestTemplate, 2); }

    @Override
    public String getUrlTemplate(int tileX, int tileY, int zoom) {
        return requestUrlTemplate.replace("{u}", BingMapManager.tileToQuadKey(tileX, tileY, zoom));
    }

    @Override
    protected int getZoomFromLevel(int level) {
        return 19 - level;
    }

	public static String tileToQuadKey(int tileX, int tileY, int zoom) {
	    StringBuilder quadKey = new StringBuilder();
	    for (int i = zoom; i > 0; i--) {
	        char digit = '0';
	        int mask = 1 << (i - 1);
	        if ((tileX & mask) != 0) digit++;
	        if ((tileY & mask) != 0) digit+=2;
	        quadKey.append(digit);
	    }
	    return quadKey.toString();
	}
}
