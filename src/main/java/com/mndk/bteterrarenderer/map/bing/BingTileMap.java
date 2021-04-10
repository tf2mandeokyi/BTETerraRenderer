package com.mndk.bteterrarenderer.map.bing;

import java.util.Map;

import com.mndk.bteterrarenderer.map.mercator.MercatorTileMap;

public class BingTileMap extends MercatorTileMap {

	public BingTileMap(String id, Map<String, Object> object) throws Exception { super(id, object); }

	@Override
	public String getUrlTemplate(int tileX, int tileY, int zoom) {
		return super.getUrlTemplate(tileX, tileY, zoom).replace("{u}", BingTileMap.tileToQuadKey(tileX, tileY, zoom));
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
