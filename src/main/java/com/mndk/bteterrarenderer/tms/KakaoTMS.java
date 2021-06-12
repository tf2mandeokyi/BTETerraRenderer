package com.mndk.bteterrarenderer.tms;

import com.mndk.bteterrarenderer.projection.Projections;
import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;

import java.util.Map;

public class KakaoTMS extends TileMapService {
	
	public KakaoTMS(String id, Map<String, Object> object) throws Exception { super(id, object); }



	@Override
	public int[] geoCoordToTileCoord(double longitude, double latitude, int zoom) throws OutOfProjectionBoundsException {
		double[] temp = Projections.KAKAO_WTM.fromGeo(longitude, latitude);
		return wtmToTile(temp[0], temp[1], zoom);
	}



	@Override
	public double[] tileCoordToGeoCoord(int tileX, int tileY, int zoom) throws OutOfProjectionBoundsException {
		double[] temp = tileToWTM(tileX, tileY, zoom);
		return Projections.KAKAO_WTM.toGeo(temp[0], temp[1]);
	}
	
	
	
	public static double[] tileToWTM(double tileX, double tileY, int level) {
		double factor = Math.pow(2, level - 3) * 256;
		return new double[] {tileX * factor - 30000, tileY * factor - 60000};
	}

	
	
	public static int[] wtmToTile(double wtmX, double wtmY, int level) {
		double divisor = Math.pow(2, level - 3) * 256;
		return new int[] {(int) Math.floor((wtmX + 30000) / divisor), (int) Math.floor((wtmY + 60000) / divisor)};
	}

}
